import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { In, Repository } from 'typeorm';
import { Role } from '../roles/role.entity';
import { CreateUserDto } from './dto/create-user.dto';
import { UpdateUserDto } from './dto/update-user.dto';
import { User } from './user.entity';

@Injectable()
export class UsersService {
  constructor(
    @InjectRepository(User)
    private readonly usersRepository: Repository<User>,
    @InjectRepository(Role)
    private readonly rolesRepository: Repository<Role>,
  ) {}

  async create(dto: CreateUserDto) {
    const { roleIds, ...payload } = dto;
    const roles = await this.resolveRoles(roleIds);
    const user = this.usersRepository.create({ ...payload, roles });
    return this.usersRepository.save(user);
  }

  findAll() {
    return this.usersRepository.find();
  }

  async findOne(id: string) {
    const user = await this.usersRepository.findOne({ where: { id } });
    if (!user) {
      throw new NotFoundException(`User with id ${id} not found`);
    }
    return user;
  }

  async update(id: string, dto: UpdateUserDto) {
    const user = await this.usersRepository.findOne({ where: { id } });
    if (!user) {
      throw new NotFoundException(`User with id ${id} not found`);
    }

    if (dto.roleIds) {
      user.roles = await this.resolveRoles(dto.roleIds);
    }

    if (dto.email !== undefined) {
      user.email = dto.email;
    }

    if (dto.fullName !== undefined) {
      user.fullName = dto.fullName;
    }

    if (dto.passwordHash !== undefined) {
      user.passwordHash = dto.passwordHash;
    }

    return this.usersRepository.save(user);
  }

  async remove(id: string) {
    const result = await this.usersRepository.delete(id);
    if (!result.affected) {
      throw new NotFoundException(`User with id ${id} not found`);
    }
  }

  private async resolveRoles(roleIds: string[]) {
    const roles = await this.rolesRepository.findBy({ id: In(roleIds) });
    if (roles.length !== roleIds.length) {
      const foundIds = new Set(roles.map((role) => role.id));
      const missingIds = roleIds.filter((id) => !foundIds.has(id));
      throw new NotFoundException(
        `Role not found for ids: ${missingIds.join(', ')}`,
      );
    }
    return roles;
  }
}
