import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Category } from '../categories/category.entity';
import { CommonModule } from '../common/common.module';
import { Product } from './product.entity';
import { ProductsController } from './products.controller';
import { ProductsService } from './products.service';

@Module({
  imports: [TypeOrmModule.forFeature([Product, Category]), CommonModule],
  controllers: [ProductsController],
  providers: [ProductsService],
})
export class ProductsModule {}
