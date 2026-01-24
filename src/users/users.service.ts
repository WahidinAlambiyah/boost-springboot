import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { Role } from '../roles/role.entity';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { UserQueryDto } from './dto/user-query.dto';
import { User } from './user.entity';

const USER_SORT_FIELDS: Array<keyof User> = [
  'email',
  'fullName',
  'createdAt',
  'updatedAt',
];

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private readonly usersRepository: Repository<User>,
    @InjectRepository(Role)
    private readonly rolesRepository: Repository<Role>,
  ) {}

  async create(dto: CreateUserDto, actor: string) {
    await this.ensureEmailAvailable(dto.email);
    const { roleIds, ...payload } = dto;
    const roles = await this.resolveRoles(roleIds);
    const user = this.usersRepository.create({
      ...payload,
      roles,
      createdBy: actor,
      updatedBy: actor,
    });
    return this.usersRepository.save(user);
  }

  async findAll(query: UserQueryDto) {
    const page = query.page;
    const limit = query.limit;
    const skip = (page - 1) * limit;
    const sortBy = this.resolveSortBy(query.sortBy);
    const sortOrder = query.sortOrder === 'DESC' ? 'DESC' : 'ASC';

    const qb = this.usersRepository.createQueryBuilder('user');

    if (query.email) {
      qb.andWhere('user.email = :email', { email: query.email });
    }

    if (query.fullName) {
      qb.andWhere('user.fullName ILIKE :fullName', {
        fullName: `%${query.fullName}%`,
      });
    }

    const [data, total] = await qb
      .orderBy(`user.${sortBy}`, sortOrder)
      .skip(skip)
      .take(limit)
      .getManyAndCount();

    return {
      data,
      meta: {
        page,
        limit,
        total,
        totalPages: Math.max(1, Math.ceil(total / limit)),
        sortBy,
        sortOrder,
      },
    };
  }

  async findOne(id: string) {
    const user = await this.usersRepository.findOne({ where: { id } });
    if (!user) {
      throw new NotFoundException(`User with id ${id} not found`);
    }
    return user;
  }

  async update(id: string, dto: UpdateUserDto, actor: string) {
    const user = await this.usersRepository.findOne({ where: { id } });
    if (!user) {
      throw new NotFoundException(`User with id ${id} not found`);
    }

    if (dto.email && dto.email !== user.email) {
      await this.ensureEmailAvailable(dto.email, id);
      user.email = dto.email;
    }

    if (dto.roleIds) {
      user.roles = await this.resolveRoles(dto.roleIds);
    }

    if (dto.fullName !== undefined) {
      user.fullName = dto.fullName;
    }

    if (dto.passwordHash !== undefined) {
      user.passwordHash = dto.passwordHash;
    }

    user.updatedBy = actor;

    return this.usersRepository.save(user);
  }

  async remove(id: string) {
    const result = await this.usersRepository.delete(id);
    if (!result.affected) {
      throw new NotFoundException(`User with id ${id} not found`);
    }
  }

  private resolveSortBy(sortBy?: string): keyof User {
    if (!sortBy) {
      return 'createdAt';
    }
    if (USER_SORT_FIELDS.includes(sortBy as keyof User)) {
      return sortBy as keyof User;
    }
    throw new BadRequestException(
      `Invalid sortBy. Allowed: ${USER_SORT_FIELDS.join(', ')}`,
    );
  }

  private async ensureEmailAvailable(email: string, excludeId?: string) {
    const existing = await this.usersRepository.findOne({ where: { email } });
    if (existing && existing.id !== excludeId) {
      throw new BadRequestException(`User email ${email} already exists`);
    }
  }

  private async resolveRoles(roleIds: string[]) {
    const roles = await this.rolesRepository.findBy({ id: In(roleIds) });
    if (roles.length !== roleIds.length) {
      const foundIds = new Set(roles.map((role) => role.id));
      const missingIds = roleIds.filter((roleId) => !foundIds.has(roleId));
      throw new NotFoundException(
        `Role not found for ids: ${missingIds.join(', ')}`,
      );
    }
    return roles;
  }
}
