import { ApiProperty } from '@nestjs/swagger';
import {
  Column,
  CreateDateColumn,
  Entity,
  ManyToMany,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
} from 'typeorm';
import { User } from '../users/user.entity';

@Entity('roles')
export class Role {
  @ApiProperty({ format: 'uuid' })
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ApiProperty({ example: 'ROLE_ADMIN' })
  @Column({ unique: true })
  code: string;

  @ApiProperty({ example: 'Administrator' })
  @Column()
  name: string;

  @ApiProperty({ type: () => User, isArray: true, required: false })
  @ManyToMany(() => User, (user) => user.roles)
  users?: User[];

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
