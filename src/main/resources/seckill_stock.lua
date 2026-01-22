-- KEYS[1]: 库存Key (例如 seckill:stock:101)
-- KEYS[2]: 用户购买记录Key (例如 seckill:bought:101)
-- ARGV[1]: 用户ID
-- ARGV[2]: 购买数量 (通常是 1)

-- 1. 校验用户是否已经买过 (限购逻辑)
if redis.call('sismember', KEYS[2], ARGV[1]) == 1 then
    return -1 -- 重复购买
end

-- 2. 校验库存是否足够
local stock = tonumber(redis.call('get', KEYS[1]))
if (stock == nil) or (stock < tonumber(ARGV[2])) then
    return 0 -- 库存不足
end

-- 3. 扣减库存 & 记录用户已买
redis.call('decrby', KEYS[1], ARGV[2])
redis.call('sadd', KEYS[2], ARGV[1])

return 1 -- 抢购成功