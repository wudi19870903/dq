package pers.di.common;

import java.util.List;

public class CListObserver<T> {

	public CListObserver()
	{
		m_listContainer = null;
		m_iBase = 0;
		m_iSize = 0;
	}

	public T get(int i) {
		// TODO Auto-generated method stub
		if(i < m_iSize)
			return m_listContainer.get(m_iBase+i);
		else
			return null;
	}
	
	public int size() {
		// TODO Auto-generated method stub
		return m_iSize;
	}

	public T begin() {
		// TODO Auto-generated method stub
		return m_listContainer.get(0);
	}
	public T end() {
		// TODO Auto-generated method stub
		return m_listContainer.get(size() - 1);
	}
	
	// 构建访问器
	public boolean build(List<T> origin, int iBase, int iSize)
	{
		m_listContainer = origin;
		m_iBase = iBase;
		m_iSize= iSize;
		return true;
	}
	public boolean build(List<T> origin)
	{
		return build(origin, 0, origin.size());
	}
	public boolean build(CListObserver<T> origin, int iBase, int iSize)
	{
		if(iBase >= 0 && iBase < origin.m_iBase + origin.m_iSize
				&& iBase + iSize <= origin.m_iSize)
		{
			m_listContainer = origin.m_listContainer;
			m_iBase = origin.m_iBase + iBase;
			m_iSize= iSize;
			return true;
		}
		else
		{
			return false;
		}
	}
	public boolean build(CListObserver<T> origin)
	{
		return build(origin, 0, origin.size());
	}
	
	private List<T> m_listContainer;
	private int m_iBase;
	private int m_iSize;
}
