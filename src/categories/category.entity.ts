import { ApiProperty } from '@nestjs/swagger';
import {
  Column,
  CreateDateColumn,
  Entity,
  OneToMany,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
} from 'typeorm';
import { Product } from '../products/product.entity';

@Entity('categories')
export class Category {
  @ApiProperty({ format: 'uuid' })
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ApiProperty({ example: 'CAT_ELECTRONICS' })
  @Column({ unique: true })
  code: string;

  @ApiProperty({ example: 'Electronics' })
  @Column({ unique: true })
  name: string;

  @ApiProperty({ type: () => Product, isArray: true, required: false })
  @OneToMany(() => Product, (product) => product.category)
  products?: Product[];

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
