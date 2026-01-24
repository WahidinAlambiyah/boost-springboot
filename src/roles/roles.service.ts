import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CreateRoleDto } from './dto/create-role.dto';
import { RoleQueryDto } from './dto/role-query.dto';
import { UpdateRoleDto } from './dto/update-role.dto';
import { Role } from './role.entity';

const ROLE_SORT_FIELDS: Array<keyof Role> = [
  'code',
  'name',
  'createdAt',
  'updatedAt',
];

@Injectable()
export class RolesService {
  constructor(
    @InjectRepository(Role)
    private readonly rolesRepository: Repository<Role>,
  ) {}

  async create(dto: CreateRoleDto, actor: string) {
    await this.ensureRoleCodeAvailable(dto.code);
    const role = this.rolesRepository.create({
      ...dto,
      createdBy: actor,
      updatedBy: actor,
    });
    return this.rolesRepository.save(role);
  }

  async findAll(query: RoleQueryDto) {
    const page = query.page;
    const limit = query.limit;
    const skip = (page - 1) * limit;
    const sortBy = this.resolveSortBy(query.sortBy);
    const sortOrder = query.sortOrder === 'DESC' ? 'DESC' : 'ASC';

    const qb = this.rolesRepository.createQueryBuilder('role');

    if (query.code) {
      qb.andWhere('role.code = :code', { code: query.code });
    }

    if (query.name) {
      qb.andWhere('role.name ILIKE :name', { name: `%${query.name}%` });
    }

    const [data, total] = await qb
      .orderBy(`role.${sortBy}`, sortOrder)
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
    const role = await this.rolesRepository.findOne({ where: { id } });
    if (!role) {
      throw new NotFoundException(`Role with id ${id} not found`);
    }
    return role;
  }

  async update(id: string, dto: UpdateRoleDto, actor: string) {
    const role = await this.rolesRepository.findOne({ where: { id } });
    if (!role) {
      throw new NotFoundException(`Role with id ${id} not found`);
    }

    if (dto.code && dto.code !== role.code) {
      await this.ensureRoleCodeAvailable(dto.code, id);
      role.code = dto.code;
    }

    if (dto.name !== undefined) {
      role.name = dto.name;
    }

    role.updatedBy = actor;

    return this.rolesRepository.save(role);
  }

  async remove(id: string) {
    const result = await this.rolesRepository.delete(id);
    if (!result.affected) {
      throw new NotFoundException(`Role with id ${id} not found`);
    }
  }

  private resolveSortBy(sortBy?: string): keyof Role {
    if (!sortBy) {
      return 'createdAt';
    }
    if (ROLE_SORT_FIELDS.includes(sortBy as keyof Role)) {
      return sortBy as keyof Role;
    }
    throw new BadRequestException(
      `Invalid sortBy. Allowed: ${ROLE_SORT_FIELDS.join(', ')}`,
    );
  }

  private async ensureRoleCodeAvailable(code: string, excludeId?: string) {
    const existing = await this.rolesRepository.findOne({ where: { code } });
    if (existing && existing.id !== excludeId) {
      throw new BadRequestException(`Role code ${code} already exists`);
    }
  }
}
