import type { Request } from 'express';

export function resolveRequesterIp(request: Request): string {
  const forwardedFor = request.headers['x-forwarded-for'];
  const forwardedIp = Array.isArray(forwardedFor)
    ? forwardedFor[0]
    : forwardedFor?.split(',')[0];
  const ip = forwardedIp?.trim() || request.ip || request.socket.remoteAddress;
  return ip ? `SYSTEM@${ip}` : 'SYSTEM@unknown';
}
