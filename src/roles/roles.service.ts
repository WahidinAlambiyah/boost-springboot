import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CreateRoleDto } from './dto/create-role.dto';
import { UpdateRoleDto } from './dto/update-role.dto';
import { Role } from './role.entity';

@Injectable()
export class RolesService {
  constructor(
    @InjectRepository(Role)
    private readonly rolesRepository: Repository<Role>,
  ) {}

  create(dto: CreateRoleDto) {
    const role = this.rolesRepository.create(dto);
    return this.rolesRepository.save(role);
  }

  findAll() {
    return this.rolesRepository.find();
  }

  async findOne(id: string) {
    const role = await this.rolesRepository.findOne({ where: { id } });
    if (!role) {
      throw new NotFoundException(`Role with id ${id} not found`);
    }
    return role;
  }

  async update(id: string, dto: UpdateRoleDto) {
    const role = await this.rolesRepository.preload({ id, ...dto });
    if (!role) {
      throw new NotFoundException(`Role with id ${id} not found`);
    }
    return this.rolesRepository.save(role);
  }

  async remove(id: string) {
    const result = await this.rolesRepository.delete(id);
    if (!result.affected) {
      throw new NotFoundException(`Role with id ${id} not found`);
    }
  }
}
