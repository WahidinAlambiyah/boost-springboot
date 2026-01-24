import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { ILike, Repository } from 'typeorm';
import { CacheToggleService } from '../common/cache/cache-toggle.service';
import { CreateCategoryDto } from './dto/create-category.dto';
import { UpdateCategoryDto } from './dto/update-category.dto';
import { CategoryQueryDto } from './dto/category-query.dto';
import { Category } from './category.entity';

const CATEGORY_SORT_FIELDS: Array<keyof Category> = [
  'code',
  'name',
  'createdAt',
  'updatedAt',
];

@Injectable()
export class CategoriesService {
  constructor(
    @InjectRepository(Category)
    private readonly categoriesRepository: Repository<Category>,
    private readonly cacheService: CacheToggleService,
  ) {}

  async create(dto: CreateCategoryDto, actor: string) {
    await this.ensureCategoryCodeAvailable(dto.code);
    const category = this.categoriesRepository.create({
      ...dto,
      createdBy: actor,
      updatedBy: actor,
    });
    const saved = await this.categoriesRepository.save(category);
    await this.invalidateCaches(saved.id);
    return saved;
  }

  async findAll(query: CategoryQueryDto) {
    const cacheKey = this.buildListCacheKey(query);
    const cached = await this.cacheService.get<unknown>(cacheKey);
    if (cached) {
      return cached as {
        data: Category[];
        meta: Record<string, unknown>;
      };
    }

    const page = query.page;
    const limit = query.limit;
    const skip = (page - 1) * limit;
    const sortBy = this.resolveSortBy(query.sortBy);
    const sortOrder = query.sortOrder === 'DESC' ? 'DESC' : 'ASC';

    const where: Record<string, unknown> = {};

    if (query.code) {
      where.code = query.code;
    }

    if (query.name) {
      where.name = ILike(`%${query.name}%`);
    }

    const [data, total] = await this.categoriesRepository.findAndCount({
      where: Object.keys(where).length ? where : undefined,
      order: { [sortBy]: sortOrder },
      skip,
      take: limit,
    });

    const response = {
      data,
      meta: {
        page,
        limit,
        total,
        totalPages: Math.max(1, Math.ceil(total / limit)),
        sortBy,
        sortOrder,
        filters: {
          code: query.code ?? null,
          name: query.name ?? null,
        },
      },
    };

    await this.cacheService.set(cacheKey, response);
    return response;
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

  async update(id: string, dto: UpdateCategoryDto, actor: string) {
    const category = await this.categoriesRepository.findOne({ where: { id } });
    if (!category) {
      throw new NotFoundException(`Category with id ${id} not found`);
    }

    if (dto.code && dto.code !== category.code) {
      await this.ensureCategoryCodeAvailable(dto.code, id);
      category.code = dto.code;
    }

    if (dto.name !== undefined) {
      category.name = dto.name;
    }

    category.updatedBy = actor;

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

  private resolveSortBy(sortBy?: string): keyof Category {
    if (!sortBy) {
      return 'createdAt';
    }
    if (CATEGORY_SORT_FIELDS.includes(sortBy as keyof Category)) {
      return sortBy as keyof Category;
    }
    throw new BadRequestException(
      `Invalid sortBy. Allowed: ${CATEGORY_SORT_FIELDS.join(', ')}`,
    );
  }

  private buildListCacheKey(query: CategoryQueryDto) {
    const params = new URLSearchParams({
      page: String(query.page),
      limit: String(query.limit),
      sortBy: query.sortBy ?? 'createdAt',
      sortOrder: query.sortOrder ?? 'ASC',
      code: query.code ?? '',
      name: query.name ?? '',
    });
    return `categories:list:${params.toString()}`;
  }

  private getCategoryCacheKey(id: string) {
    return `categories:${id}`;
  }

  private async invalidateCaches(id: string) {
    await Promise.all([
      this.cacheService.del(this.getCategoryCacheKey(id)),
      this.cacheService.reset(),
    ]);
  }

  private async ensureCategoryCodeAvailable(code: string, excludeId?: string) {
    const existing = await this.categoriesRepository.findOne({ where: { code } });
    if (existing && existing.id !== excludeId) {
      throw new BadRequestException(`Category code ${code} already exists`);
    }
  }
}
