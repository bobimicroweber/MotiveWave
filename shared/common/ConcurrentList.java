package com.motivewave.platform.study.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.motivewave.platform.sdk.common.Util;

/**
 * The purpose of this class is to provide a list structure that allows the user to read the 
 * list contents without having to worry about getting a ConcurrentModificationException if the list is modified
 * An internal readonly list is maintained and returned via the get() method.
 * Any changes to the list will result in a the creation of a new list which is assigned to the internal list at the end of the operation.
 * This is useful if the list is read often but only modified occasionally.
 */
public class ConcurrentList<T> implements Cloneable
{
  public ConcurrentList() {}
  public ConcurrentList(List<T> items)
  {
    this.items = new ArrayList(items);
  }

  public void reverse()
  {
    if (items == null || items == Collections.EMPTY_LIST || items.isEmpty()) return;
    synchronized(this) {
      List<T> _items = new ArrayList<>(items); 
      Collections.reverse(_items);
      items = _items;
    }
  }
  
  public void addAll(List<T> list)
  {
    if (Util.isEmpty(list)) return;
    synchronized(this) {
      for(T l : list) { add(l); }
    }
  }

  public void addAllNoCheck(List<T> list)
  {
    if (Util.isEmpty(list)) return;
    synchronized(this) {
      if (isEmpty()) items = new ArrayList<>(list);
      else items.addAll(list);
    }
  }

  public void setAll(List<T> list)
  {
    clear();
    if (Util.isEmpty(list)) return;
    synchronized(this) {
      for(T l : list) { add(l); }
    }
  }
  
  public void setAllNoCheck(List<T> list)
  {
    items = list == null ? Collections.EMPTY_LIST : list;
  }
  
  public void add(T l)
  {
    if (l == null) return;
    synchronized(this) {
      if (items.contains(l)) return;
      List<T> list = items == Collections.EMPTY_LIST ? new ArrayList(1) : new ArrayList(items);
      list.add(l);
      items = list;
    }
  }

  // Adds without checking if the element already exists.
  public void addNoCheck(T l)
  {
    if (l == null) return;
    synchronized(this) {
      List<T> list = items == Collections.EMPTY_LIST ? new ArrayList(1) : new ArrayList(items);
      list.add(l);
      items = list;
    }
  }

  public void replace(T l)
  {
    if (l == null) return;
    synchronized(this) {
      List<T> list = items == Collections.EMPTY_LIST ? new ArrayList(1) : new ArrayList(items);
      if (!list.contains(l)) list.add(l);
      else {
        int ind = list.indexOf(l);
        list.remove(l);
        list.add(ind, l);
      }
      items = list;
    }
  }

  public void add(int index, T l)
  {
    if (l == null) return;
    synchronized(this) {
      if (items.contains(l)) return;
      List<T> list = items == Collections.EMPTY_LIST ? new ArrayList(1) : new ArrayList(items);
      list.add(index, l);
      items = list;
    }
  }

  // Adds without checking to see if the element already exists.
  public void addNoCheck(int index, T l)
  {
    if (l == null) return;
    synchronized(this) {
      List<T> list = items == Collections.EMPTY_LIST ? new ArrayList(1) : new ArrayList(items);
      list.add(index, l);
      items = list;
    }
  }

  public void set(List<T> list)
  {
    synchronized(this) {
      ArrayList<T> tmp = new ArrayList(list);
      tmp.trimToSize();
      items = tmp;
    }
  }

  public boolean remove(T l) 
  {
    synchronized(this) {
      if (!items.contains(l)) return false;
      ArrayList<T> list = new ArrayList(items);
      list.remove(l);
      list.trimToSize();
      items = list;
    }
    return true;
  }

  public void trimToSize(int minLimit, int maxLimit)
  {
    synchronized(this) {
      if (items.size() < maxLimit) return;
      ArrayList<T> list = new ArrayList(maxLimit);
      for(int i=0; i < minLimit; i++) {
        list.add(items.get(i));
      }
      items = list;
    }
  }
  
  public void clear()
  {
    synchronized(this) { items = Collections.EMPTY_LIST; }
  }
  
  public void sort(Comparator<T> c)
  {
    synchronized(this) {
      items.sort(c);
    }
  }

  public T get(int index) { return items.get(index); }
  public List<T> get() { return items; }
  public int indexOf(T o) { return items.indexOf(o); }
  public boolean contains(T o) { return items.contains(o); }
  public int size() { return items == null ? 0 : items.size(); }
  public boolean isEmpty() { return Util.isEmpty(items); }
  
  @Override
  public ConcurrentList<T> clone()
  {
    ConcurrentList<T> clone = new ConcurrentList();
    clone.items = new ArrayList(items);
    return clone;
  }
  
  private List<T> items = Collections.EMPTY_LIST;
}
