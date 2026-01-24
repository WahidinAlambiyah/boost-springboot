import { ApiPropertyOptional } from '@nestjs/swagger';
import { IsOptional, IsString, Matches } from 'class-validator';
import { PaginationQueryDto } from '../../common/pagination-query.dto';

export class RoleQueryDto extends PaginationQueryDto {
  @ApiPropertyOptional({ description: 'Filter by role code' })
  @IsOptional()
  @IsString()
  @Matches(/^[A-Z0-9_]+$/)
  code?: string;

  @ApiPropertyOptional({ description: 'Filter by role name (partial match)' })
  @IsOptional()
  @IsString()
  name?: string;
}
