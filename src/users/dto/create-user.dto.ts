import { ApiProperty } from '@nestjs/swagger';
import {
  ArrayNotEmpty,
  IsArray,
  IsEmail,
  IsNotEmpty,
  IsString,
  IsUUID,
} from 'class-validator';

export class CreateUserDto {
  @ApiProperty({ example: 'user@example.com' })
  @IsEmail()
  email: string;

  @ApiProperty({ example: 'Jane Doe' })
  @IsString()
  @IsNotEmpty()
  fullName: string;

  @ApiProperty({ example: 'hashed-password-placeholder' })
  @IsString()
  @IsNotEmpty()
  passwordHash: string;

  @ApiProperty({
    type: [String],
    format: 'uuid',
    example: ['b5d1b6e2-6a3b-4d62-9d9e-0e1c6ed0f9bb'],
  })
  @IsArray()
  @ArrayNotEmpty()
  @IsUUID('4', { each: true })
  roleIds: string[];
}
