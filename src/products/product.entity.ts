import { ApiProperty } from '@nestjs/swagger';
import {
  Column,
  CreateDateColumn,
  Entity,
  ManyToOne,
  PrimaryGeneratedColumn,
  UpdateDateColumn,
} from 'typeorm';
import { Category } from '../categories/category.entity';

const priceTransformer = {
  to: (value: number) => value,
  from: (value: string) => Number(value),
};

@Entity('products')
export class Product {
  @ApiProperty({ format: 'uuid' })
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ApiProperty({ example: 'PROD_LAPTOP' })
  @Column({ unique: true })
  code: string;

  @ApiProperty({ example: 'Laptop' })
  @Column()
  name: string;

  @ApiProperty({ example: 999.99 })
  @Column({ type: 'numeric', transformer: priceTransformer })
  price: number;

  @ApiProperty({ type: () => Category })
  @ManyToOne(() => Category, (category) => category.products, {
    eager: true,
    nullable: false,
  })
  category: Category;

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
