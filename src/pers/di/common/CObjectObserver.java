package pers.di.common;

public class CObjectObserver<T> {
	public CObjectObserver()
	{
		m_object = null;
	}
	
	public boolean build(T origin)
	{
		m_object = origin;
		return true;
	}
	public boolean build(CObjectObserver<T> origin)
	{
		m_object = origin.m_object;
		return true;
	}
	
	public T get()
	{
		return m_object;
	}
	private T m_object;
}
