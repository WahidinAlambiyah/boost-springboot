import { ApiProperty } from '@nestjs/swagger';
import {
  Column,
  CreateDateColumn,
  Entity,
  JoinTable,
  ManyToMany,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
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

  @ApiProperty()
  @CreateDateColumn({ name: 'created_at' })
  createdAt: Date;

  @ApiProperty()
  @UpdateDateColumn({ name: 'updated_at' })
  updatedAt: Date;

  @ApiProperty({ example: 'SYSTEM@127.0.0.1' })
  @Column({ name: 'created_by', default: 'SYSTEM@unknown' })
  createdBy: string;

  @ApiProperty({ example: 'SYSTEM@127.0.0.1' })
  @Column({ name: 'updated_by', default: 'SYSTEM@unknown' })
  updatedBy: string;
}
