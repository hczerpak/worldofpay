package cache

/**
  * A simple data storage abstraction
  */
trait Cache[T] {
  def del(key: String)
  def set(key: String, value: T): T
  def get(key: String): Option[T]
}
