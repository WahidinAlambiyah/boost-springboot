import {
  BadRequestException,
  Injectable,
  NotFoundException,
} from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { Category } from '../categories/category.entity';
import { CacheToggleService } from '../common/cache/cache-toggle.service';
import { CreateProductDto } from './dto/create-product.dto';
import { UpdateProductDto } from './dto/update-product.dto';
import { ProductQueryDto } from './dto/product-query.dto';
import { Product } from './product.entity';

const PRODUCT_SORT_FIELDS: Array<keyof Product> = [
  'code',
  'name',
  'price',
  'createdAt',
  'updatedAt',
];

@Injectable()
export class ProductsService {
  constructor(
    @InjectRepository(Product)
    private readonly productsRepository: Repository<Product>,
    @InjectRepository(Category)
    private readonly categoriesRepository: Repository<Category>,
    private readonly cacheService: CacheToggleService,
  ) {}

  async create(dto: CreateProductDto, actor: string) {
    await this.ensureProductCodeAvailable(dto.code);
    const category = await this.findCategory(dto.categoryId);
    const product = this.productsRepository.create({
      code: dto.code,
      name: dto.name,
      price: dto.price,
      category,
      createdBy: actor,
      updatedBy: actor,
    });
    const saved = await this.productsRepository.save(product);
    await this.invalidateCaches(saved.id);
    return saved;
  }

  async findAll(query: ProductQueryDto) {
    const cacheKey = this.buildListCacheKey(query);
    const cached = await this.cacheService.get<unknown>(cacheKey);
    if (cached) {
      return cached as {
        data: Product[];
        meta: Record<string, unknown>;
      };
    }

    const page = query.page;
    const limit = query.limit;
    const skip = (page - 1) * limit;
    const sortBy = this.resolveSortBy(query.sortBy);
    const sortOrder = query.sortOrder === 'DESC' ? 'DESC' : 'ASC';

    const qb = this.productsRepository.createQueryBuilder('product');

    if (query.code) {
      qb.andWhere('product.code = :code', { code: query.code });
    }

    if (query.name) {
      qb.andWhere('product.name ILIKE :name', { name: `%${query.name}%` });
    }

    if (query.categoryId) {
      qb.andWhere('product.categoryId = :categoryId', {
        categoryId: query.categoryId,
      });
    }

    if (query.minPrice) {
      qb.andWhere('product.price >= :minPrice', {
        minPrice: Number(query.minPrice),
      });
    }

    if (query.maxPrice) {
      qb.andWhere('product.price <= :maxPrice', {
        maxPrice: Number(query.maxPrice),
      });
    }

    const [data, total] = await qb
      .orderBy(`product.${sortBy}`, sortOrder)
      .skip(skip)
      .take(limit)
      .getManyAndCount();

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
          categoryId: query.categoryId ?? null,
          minPrice: query.minPrice ?? null,
          maxPrice: query.maxPrice ?? null,
        },
      },
    };

    await this.cacheService.set(cacheKey, response);
    return response;
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

  async update(id: string, dto: UpdateProductDto, actor: string) {
    const product = await this.productsRepository.findOne({ where: { id } });
    if (!product) {
      throw new NotFoundException(`Product with id ${id} not found`);
    }

    if (dto.code && dto.code !== product.code) {
      await this.ensureProductCodeAvailable(dto.code, id);
      product.code = dto.code;
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

    product.updatedBy = actor;

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

  private resolveSortBy(sortBy?: string): keyof Product {
    if (!sortBy) {
      return 'createdAt';
    }
    if (PRODUCT_SORT_FIELDS.includes(sortBy as keyof Product)) {
      return sortBy as keyof Product;
    }
    throw new BadRequestException(
      `Invalid sortBy. Allowed: ${PRODUCT_SORT_FIELDS.join(', ')}`,
    );
  }

  private buildListCacheKey(query: ProductQueryDto) {
    const params = new URLSearchParams({
      page: String(query.page),
      limit: String(query.limit),
      sortBy: query.sortBy ?? 'createdAt',
      sortOrder: query.sortOrder ?? 'ASC',
      code: query.code ?? '',
      name: query.name ?? '',
      categoryId: query.categoryId ?? '',
      minPrice: query.minPrice ?? '',
      maxPrice: query.maxPrice ?? '',
    });
    return `products:list:${params.toString()}`;
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
      this.cacheService.del(this.getProductCacheKey(id)),
      this.cacheService.reset(),
    ]);
  }

  private async ensureProductCodeAvailable(code: string, excludeId?: string) {
    const existing = await this.productsRepository.findOne({ where: { code } });
    if (existing && existing.id !== excludeId) {
      throw new BadRequestException(`Product code ${code} already exists`);
    }
  }
}
