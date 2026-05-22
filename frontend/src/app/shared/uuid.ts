export type Id = number;

export function uuid(): Id {
  return Math.floor(Math.random() * 1_000_000_000);
}
