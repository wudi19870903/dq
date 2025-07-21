package pers.di.common;

public class CObjectContainer<T> {
	public CObjectContainer()
	{
		m_object = null;
	}
	public void set(T obj)
	{
		m_object = obj;
	}
	public T get()
	{
		return m_object;
	}
	public T move()
	{
		T tmp = m_object;
		m_object = null;
		return tmp;
	}
	private T m_object;
}
