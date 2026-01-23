import { ApiProperty } from '@nestjs/swagger';
import {
  Column,
  Entity,
  JoinTable,
  ManyToMany,
  PrimaryGeneratedColumn,
} from 'typeorm';
import { Role } from '../roles/role.entity';

@Entity('users')
export class User {
  @ApiProperty({ format: 'uuid' })
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ApiProperty({ example: 'user@example.com' })
  @Column({ unique: true })
  email: string;

  @ApiProperty({ example: 'Jane Doe' })
  @Column()
  fullName: string;

  @ApiProperty({ example: 'hashed-password-placeholder' })
  @Column()
  passwordHash: string;

  @ApiProperty({ type: () => Role, isArray: true })
  @ManyToMany(() => Role, (role) => role.users, { eager: true })
  @JoinTable({ name: 'user_roles' })
  roles: Role[];
}
