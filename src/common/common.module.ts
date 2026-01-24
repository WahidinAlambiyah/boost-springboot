import { Module } from '@nestjs/common';
import { CacheToggleService } from './cache/cache-toggle.service';

@Module({
  providers: [CacheToggleService],
  exports: [CacheToggleService],
})
export class CommonModule {}
