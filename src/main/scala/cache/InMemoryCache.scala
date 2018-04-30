package cache

import java.util.concurrent.ConcurrentHashMap

class InMemoryCache[T] extends Cache[T]{
    val cache: java.util.Map[String, T] = new ConcurrentHashMap[String, T]()
    def del(key: String) = cache.remove(key)
    def set(key: String, value: T): T = cache.put(key, value)
    def get(key: String): Option[T] = Option(cache.get(key))
}
