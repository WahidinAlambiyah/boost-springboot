import { ApiProperty } from '@nestjs/swagger';
import { Type } from 'class-transformer';
import { IsNotEmpty, IsNumber, IsString, IsUUID } from 'class-validator';

export class CreateProductDto {
  @ApiProperty({ example: 'Laptop' })
  @IsString()
  @IsNotEmpty()
  name: string;

  @ApiProperty({ example: 999.99 })
  @Type(() => Number)
  @IsNumber()
  price: number;

  @ApiProperty({ format: 'uuid' })
  @IsUUID('4')
  categoryId: string;
}
