import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, Matches } from 'class-validator';
import { PaginationQueryDto } from '../../common/pagination-query.dto';

export class CategoryQueryDto extends PaginationQueryDto {
  @ApiPropertyOptional({ description: 'Filter by category code' })
  @IsOptional()
  @IsString()
  @Matches(/^[A-Z0-9_]+$/)
  code?: string;

  @ApiPropertyOptional({ description: 'Filter by category name (partial match)' })
  @IsOptional()
  @IsString()
  name?: string;
}
