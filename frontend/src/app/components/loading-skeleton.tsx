interface LoadingSkeletonProps {
  rows?: number;
  className?: string;
}

export default function LoadingSkeleton({ rows = 3, className = "" }: LoadingSkeletonProps) {
  return (
    <div className={`mt-4 animate-pulse space-y-3 ${className}`.trim()}>
      {Array.from({ length: rows }).map((_, index) => (
        <div key={`skeleton-row-${index}`} className="h-10 w-full rounded-md bg-zinc-200" />
      ))}
    </div>
  );
}
