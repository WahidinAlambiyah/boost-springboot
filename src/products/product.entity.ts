import { ApiProperty } from '@nestjs/swagger';
import { Column, Entity, ManyToOne, PrimaryGeneratedColumn } from 'typeorm';
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
}
