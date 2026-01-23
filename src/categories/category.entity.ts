import { ApiProperty } from '@nestjs/swagger';
import { Column, Entity, OneToMany, PrimaryGeneratedColumn } from 'typeorm';
import { Product } from '../products/product.entity';

@Entity('categories')
export class Category {
  @ApiProperty({ format: 'uuid' })
  @PrimaryGeneratedColumn('uuid')
  id: string;

  @ApiProperty({ example: 'Electronics' })
  @Column({ unique: true })
  name: string;

  @ApiProperty({ type: () => Product, isArray: true, required: false })
  @OneToMany(() => Product, (product) => product.category)
  products?: Product[];
}
