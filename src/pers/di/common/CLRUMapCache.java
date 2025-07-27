package pers.di.common;

import java.util.*;

/*
 * Least Recently Used
 * 最近最少使用 丢弃缓存
 */
public class CLRUMapCache<K, V> {
	
	public CLRUMapCache(int cacheSize)
	{
		m_currentSize = 0;  
		m_cacheSize = cacheSize;  
		m_nodes = new Hashtable<Object, Entry>(cacheSize);//缓存容器  
	}
	
    /** 
     * 判断是否有key 
     */  
    public boolean containsKey(K key) {  
    	return m_nodes.containsKey(key);
    }  
	
	/** 
     * 获取缓存中对象,并把它放在最前面 
     */  
    public V get(K key) {  
        Entry node = m_nodes.get(key);  
        if (node != null) {  
            moveToHead(node);  
            return (V)node.value;  
        } else {  
            return null;  
        }  
    }  
      
    /** 
     * 添加 entry到hashtable, 并把entry  
     */  
    public void put(K key, V value) {  
        //先查看hashtable是否存在该entry, 如果存在，则只更新其value  
        Entry node = m_nodes.get(key);  
          
        if (node == null) {  
            //缓存容器是否已经超过大小.  
            if (m_currentSize >= m_cacheSize) {  
            	m_nodes.remove(m_last.key);  
                removeLast();  
            } else {  
            	m_currentSize++;  
            }             
            node = new Entry();  
        }  
        node.key = key;
        node.value = value;  
        //将最新使用的节点放到链表头，表示最新使用的.  
        moveToHead(node);  
        m_nodes.put(key, node);  
    }  
    
    /** 
     * 将entry删除, 注意：删除操作只有在cache满了才会被执行 
     */  
    public void remove(K key) {  
        Entry node = m_nodes.get(key);  
        //在链表中删除  
        if (node != null) {  
            if (node.prev != null) {  
                node.prev.next = node.next;  
            }  
            if (node.next != null) {  
                node.next.prev = node.prev;  
            }  
            if (m_last == node)  
            	m_last = node.prev;  
            if (m_first == node)  
            	m_first = node.next;  
        }  
        //在hashtable中删除  
        m_nodes.remove(key);  
    }  
    
    /**
     * 清空缓存 
     */  
    public void clear() {  
    	m_first = null;  
    	m_last = null;  
        m_currentSize = 0;  
    }  
    
    /***********************************************************************************/
  
    /** 
     * 删除链表尾部节点，即使用最后 使用的entry 
     */  
    private void removeLast() {  
        //链表尾不为空,则将链表尾指向null. 删除连表尾（删除最少使用的缓存对象）  
        if (m_last != null) {  
            if (m_last.prev != null)  
            	m_last.prev.next = null;  
            else  
            	m_first = null;  
            m_last = m_last.prev;  
        }  
    }  
      
    /** 
     * 移动到链表头，表示这个节点是最新使用过的 
     */  
    private void moveToHead(Entry node) {  
        if (node == m_first)  
            return;  
        if (node.prev != null)  
            node.prev.next = node.next;  
        if (node.next != null)  
            node.next.prev = node.prev;  
        if (m_last == node)  
        	m_last = node.prev;  
        if (m_first != null) {  
            node.next = m_first;  
            m_first.prev = node;  
        }  
        m_first = node;  
        node.prev = null;  
        if (m_last == null)  
        	m_last = m_first;  
    }  
  
	class Entry {  
	    Entry prev;//前一节点  
	    Entry next;//后一节点  
	    Object value;//值  
	    Object key;//键  
	}  
	
	private int m_cacheSize;  
    private Hashtable<Object, Entry> m_nodes;//缓存容器  
    private int m_currentSize;  
    private Entry m_first;//链表头  
    private Entry m_last;//链表尾
}
