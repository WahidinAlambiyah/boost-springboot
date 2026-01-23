import { Injectable, NotFoundException } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { CacheToggleService } from '../common/cache/cache-toggle.service';
import { CreateCategoryDto } from './dto/create-category.dto';
import { UpdateCategoryDto } from './dto/update-category.dto';
import { Category } from './category.entity';

@Injectable()
export class CategoriesService {
  private readonly listCacheKey = 'categories:all';

  constructor(
    @InjectRepository(Category)
    private readonly categoriesRepository: Repository<Category>,
    private readonly cacheService: CacheToggleService,
  ) {}

  async create(dto: CreateCategoryDto) {
    const category = this.categoriesRepository.create(dto);
    const saved = await this.categoriesRepository.save(category);
    await this.invalidateCaches(saved.id);
    return saved;
  }

  async findAll() {
    const cached = await this.cacheService.get<Category[]>(this.listCacheKey);
    if (cached) {
      return cached;
    }

    const categories = await this.categoriesRepository.find();
    await this.cacheService.set(this.listCacheKey, categories);
    return categories;
  }

  async findOne(id: string) {
    const cacheKey = this.getCategoryCacheKey(id);
    const cached = await this.cacheService.get<Category>(cacheKey);
    if (cached) {
      return cached;
    }

    const category = await this.categoriesRepository.findOne({ where: { id } });
    if (!category) {
      throw new NotFoundException(`Category with id ${id} not found`);
    }

    await this.cacheService.set(cacheKey, category);
    return category;
  }

  async update(id: string, dto: UpdateCategoryDto) {
    const category = await this.categoriesRepository.preload({ id, ...dto });
    if (!category) {
      throw new NotFoundException(`Category with id ${id} not found`);
    }
    const saved = await this.categoriesRepository.save(category);
    await this.invalidateCaches(id);
    return saved;
  }

  async remove(id: string) {
    const result = await this.categoriesRepository.delete(id);
    if (!result.affected) {
      throw new NotFoundException(`Category with id ${id} not found`);
    }
    await this.invalidateCaches(id);
  }

  private getCategoryCacheKey(id: string) {
    return `categories:${id}`;
  }

  private async invalidateCaches(id: string) {
    await Promise.all([
      this.cacheService.del(this.listCacheKey),
      this.cacheService.del(this.getCategoryCacheKey(id)),
    ]);
  }
}
