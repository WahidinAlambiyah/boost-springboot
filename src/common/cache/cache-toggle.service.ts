import { Inject, Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { CACHE_MANAGER } from '@nestjs/cache-manager';
import type { Cache } from 'cache-manager';

@Injectable()
export class CacheToggleService {
  private readonly cacheEnabled: boolean;
  private readonly redisEnabled: boolean;
  private readonly defaultTtlMs: number;

  constructor(
    @Inject(CACHE_MANAGER) private readonly cacheManager: Cache,
    private readonly configService: ConfigService,
  ) {
    this.cacheEnabled =
      this.configService.get<string>('CACHE_ENABLED') === 'true';
    this.redisEnabled =
      this.configService.get<string>('REDIS_ENABLED') === 'true';
    this.defaultTtlMs = Number(
      this.configService.get<string>('CACHE_TTL_MS') ?? 60000,
    );
  }

  async get<T>(key: string): Promise<T | undefined> {
    if (!this.cacheEnabled) {
      return undefined;
    }

    const value = await this.cacheManager.get<T>(key);
    return value ?? undefined;
  }

  async set<T>(key: string, value: T, ttlMs?: number): Promise<void> {
    if (!this.cacheEnabled) {
      return;
    }

    const ttl = ttlMs ?? this.defaultTtlMs;
    await this.cacheManager.set(key, value, ttl);
  }

  async del(key: string): Promise<void> {
    if (!this.cacheEnabled) {
      return;
    }

    await this.cacheManager.del(key);
  }

  async reset(): Promise<void> {
    if (!this.cacheEnabled) {
      return;
    }
    const resetFn = (this.cacheManager as { reset?: () => Promise<void> }).reset;
    if (resetFn) {
      await resetFn.call(this.cacheManager);
    }
  }

  getStatus() {
    return {
      cacheEnabled: this.cacheEnabled,
      redisEnabled: this.redisEnabled,
      defaultTtlMs: this.defaultTtlMs,
    };
  }
}
