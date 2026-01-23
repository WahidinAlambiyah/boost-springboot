import { ApiProperty } from '@nestjs/swagger';
import { Column, Entity, ManyToMany, PrimaryGeneratedColumn } from 'typeorm';
import { User } from '../users/user.entity';

@Entity('roles')
export class Role {
  @ApiProperty({ format: 'uuid' })
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ApiProperty()
  @Column({ unique: true })
  name: string;

  @ApiProperty({ type: () => User, isArray: true, required: false })
  @ManyToMany(() => User, (user) => user.roles)
  users?: User[];
}
