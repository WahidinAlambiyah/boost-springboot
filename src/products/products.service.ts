import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Category } from '../categories/category.entity';
import { CacheToggleService } from '../common/cache/cache-toggle.service';
import { CreateProductDto } from './dto/create-product.dto';
import { UpdateProductDto } from './dto/update-product.dto';
import { Product } from './product.entity';

@Injectable()
export class ProductsService {
  private readonly listCacheKey = 'products:all';

  constructor(
    @InjectRepository(Product)
    private readonly productsRepository: Repository<Product>,
    @InjectRepository(Category)
    private readonly categoriesRepository: Repository<Category>,
    private readonly cacheService: CacheToggleService,
  ) {}

  async create(dto: CreateProductDto) {
    const category = await this.findCategory(dto.categoryId);
    const product = this.productsRepository.create({
      name: dto.name,
      price: dto.price,
      category,
    });
    const saved = await this.productsRepository.save(product);
    await this.invalidateCaches(saved.id);
    return saved;
  }

  async findAll() {
    const cached = await this.cacheService.get<Product[]>(this.listCacheKey);
    if (cached) {
      return cached;
    }

    const products = await this.productsRepository.find();
    await this.cacheService.set(this.listCacheKey, products);
    return products;
  }

  async findOne(id: string) {
    const cacheKey = this.getProductCacheKey(id);
    const cached = await this.cacheService.get<Product>(cacheKey);
    if (cached) {
      return cached;
    }

    const product = await this.productsRepository.findOne({ where: { id } });
    if (!product) {
      throw new NotFoundException(`Product with id ${id} not found`);
    }

    await this.cacheService.set(cacheKey, product);
    return product;
  }

  async update(id: string, dto: UpdateProductDto) {
    const product = await this.productsRepository.findOne({ where: { id } });
    if (!product) {
      throw new NotFoundException(`Product with id ${id} not found`);
    }

    if (dto.categoryId) {
      product.category = await this.findCategory(dto.categoryId);
    }

    if (dto.name !== undefined) {
      product.name = dto.name;
    }

    if (dto.price !== undefined) {
      product.price = dto.price;
    }

    const saved = await this.productsRepository.save(product);
    await this.invalidateCaches(id);
    return saved;
  }

  async remove(id: string) {
    const result = await this.productsRepository.delete(id);
    if (!result.affected) {
      throw new NotFoundException(`Product with id ${id} not found`);
    }
    await this.invalidateCaches(id);
  }

  private async findCategory(categoryId: string) {
    const category = await this.categoriesRepository.findOne({
      where: { id: categoryId },
    });
    if (!category) {
      throw new NotFoundException(`Category with id ${categoryId} not found`);
    }
    return category;
  }

  private getProductCacheKey(id: string) {
    return `products:${id}`;
  }

  private async invalidateCaches(id: string) {
    await Promise.all([
      this.cacheService.del(this.listCacheKey),
      this.cacheService.del(this.getProductCacheKey(id)),
    ]);
  }
}
