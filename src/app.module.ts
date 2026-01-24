import { CacheModule } from '@nestjs/cache-manager';
import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { Cacheable } from 'cacheable';
import Keyv from 'keyv';
import KeyvRedis from '@keyv/redis';
import { CategoriesModule } from './categories/categories.module';
import { CommonModule } from './common/common.module';
import { ProductsModule } from './products/products.module';
import { RolesModule } from './roles/roles.module';
import { UsersModule } from './users/users.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
    }),
    CacheModule.registerAsync({
      isGlobal: true,
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => {
        const ttl = Number(
          configService.get<string>('CACHE_TTL_MS') ?? 60000,
        );
        const cacheEnabled =
          configService.get<string>('CACHE_ENABLED') === 'true';
        const redisEnabled =
          configService.get<string>('REDIS_ENABLED') === 'true';

        if (!cacheEnabled) {
          return {
            ttl,
            stores: new Cacheable({ ttl }),
          };
        }

        if (redisEnabled) {
          const redisUrl =
            configService.get<string>('REDIS_URL') ?? 'redis://localhost:6379';
          const keyv = new Keyv({
            store: new KeyvRedis(redisUrl),
          });
          return {
            ttl,
            stores: new Cacheable({ primary: keyv, ttl }),
          };
        }

        return {
          ttl,
          stores: new Cacheable({ ttl }),
        };
      },
    }),
    TypeOrmModule.forRootAsync({
      inject: [ConfigService],
      useFactory: (configService: ConfigService) => ({
        type: 'postgres',
        host: configService.get<string>('DB_HOST'),
        port: Number(configService.get<string>('DB_PORT')),
        username: configService.get<string>('DB_USER'),
        password: configService.get<string>('DB_PASSWORD'),
        database: configService.get<string>('DB_NAME'),
        schema: configService.get<string>('DB_SCHEMA') ?? 'public',
        uuidExtension: 'pgcrypto',
        synchronize: true,
        autoLoadEntities: true,
      }),
    }),
    CommonModule,
    RolesModule,
    UsersModule,
    CategoriesModule,
    ProductsModule,
  ],
})
export class AppModule {}
