/** @file */
#ifndef __ARRAYLIST_H
#define __ARRAYLIST_H
#include "IndexOutOfBound.h"
#include "ElementNotExist.h"
#include"__xyz2606__memory.h"
#include<cstdio>
/**
 * The ArrayList is just like vector in C++.
 * You should know that "capacity" here doesn't mean how many elements are now in this list, where it means
 * the length of the array of your internal implemention
 *
 * The iterator iterates in the order of the elements being loaded into this list
 */
template <class T>
class ArrayList
{
	T * op;
	T * ed;
	T * cap;
public:
    class Iterator
    {
	    T * p, *& op, *& ed;
	    bool DEL;
    public:
        /**
         * TODO Returns true if the iteration has more elements.
        */
        Iterator (T * p, T *& op, T *& ed) : p(p), op(op), ed(ed), DEL(false){}
	bool hasNext()
	{
		return p < ed;
	}
	bool operator == (const Iterator & that)
	{
		return p == that.p and op == that.op;
	}
	int getPosi()
	{
		return p - op;
	}
        /**
         * TODO Returns the next element in the iteration.
         * @throw ElementNotExist exception when hasNext() == false
         */
        const T &next()
	{
		if(p == ed) throw ElementNotExist(); else {DEL = true; return *(p++);}
	}

        /**
         * TODO Removes from the underlying collection the last element
         * returned by the iterator
         * The behavior of an iterator is unspecified if the underlying
         * collection is modifi
	 * ed while the iteration is in progress in
         * any way other than by calling this method.
         * @throw ElementNotExist
         */
        void remove()
	{
		if(!DEL) throw ElementNotExist();
		else
		{
			for(T * j = p; j != ed; j++) *(p - 1) = *p;
			ed--;
			p--;
			//printf("!%d\n", op);
			DEL = false;
		}
	}
    };

    /**
     * TODO Constructs an empty array list.
     */
    ArrayList() : op(0), ed(0), cap(0)
    {
    }
    void sawp(int x, int y)
    {
	    swp(op[x], op[y]);
    }

    /**
     * TODO Destructor
     */
    void deallocate()
    {
	    //printf("%d\n", op);
	   // printf("DE1\n");
	    if(!op) return;
	   // printf("DE2\n");
	    delete []op;
	   // printf("DE3\n");
	    
    }
    ~ArrayList()
    {
	    deallocate();
    }

    /**
     * TODO Assignment operator
     */
public:
    int capacity()
    {
	    return cap - op;
    }
    public:
    ArrayList& operator=(const ArrayList& x)
    {
	    deallocate();
	    op = new T[x.capacity()];
	    uninitialized_copy(x.op, x.ed, op);
	    ed = op + (x.ed - x.op);
	    cap = op + x.capacity();
	    return * this;
    }

    /**
     * TODO Copy-constructor
     */
    ArrayList(const ArrayList& x)
    {
	    op = new T[x.capacity()];
	    uninitialized_copy(x.op, x.ed, op);
	    ed = op + (x.ed - x.op);
	    cap = op + x.capacity();
    }

    /**
     * TODO Appends the specified element to the end of this list.
     * Always returns true.
     */
    private:
    void reallocate()
    {
	    ptrdiff_t size = ed - op;
	    ptrdiff_t newcap = 2 * (size?size:1);
	    T * newop = new T[newcap];
	    uninitialized_copy(op, ed, newop);
	    deallocate();
	    op = newop;
	    ed = newop + size;
	    cap = newop + newcap;
    }
    public:
    bool add(const T& e)
    {
	    if(ed == cap)
		    reallocate();
	    new (ed) T(e);
	    ++ed;
	    return true;
    }

    /**
     * TODO Inserts the specified element to the specified position in this list.
     * The range of index parameter is [0, size], where index=0 means inserting to the head,
     * and index=size means appending to the end.
     * @throw IndexOutOfBound
     */
     /**
     * TODO Returns the number of elements in this list.
     */
    int size() const
    {
	    return ed - op;
    }
    int capacity() const
    {
	    return cap - op;
    }
    void add(int index, const T& element)
    {
	    if(index < 0 or index > size()) throw IndexOutOfBound();
	    if(ed == cap)
		    reallocate();
	    for(T * p = ed; p != op + index; p--)
		    new (p) T(*(p - 1));
	    new (op + index) T(element);
	    ed++;
    }

    /**
     * TODO Removes all of the elements from this list.
     */
    void clear()
    {
	    deallocate();
	    op = ed = cap = 0;
    }

    /**
     * TODO Returns true if this list contains the specified element.
     */
    bool contains(const T& e) const
    {
	    for(T * p = op; p != ed; p++) if(*p == e) return true;
	    return false;
    }

    /**
     * TODO Returns a const reference to the element at the specified position in this list.
     * The index is zero-based, with range [0, size).
     * @throw IndexOutOfBound
     */
    const T& get(int index) const
    {
	    if(index < 0 or index >= size()) throw IndexOutOfBound();
	    return *(op + index);
    }

    /**
     * TODO Returns true if this list contains no elements.
     */
    bool isEmpty() const
    {
	    return op == ed;
    }

    /**
     * TODO Removes the element at the specified position in this list.
     * The index is zero-based, with range [0, size).
     * @throw IndexOutOfBound
     */
    void removeIndex(int index)
    {
	    if(index < 0 or index >= size()) throw IndexOutOfBound();
	    for(T * p = op + index; p + 1 != ed; p++) 
		    new (p) T(*(p + 1));
	    ed->~T();
	    ed--;
    }

    /**
     * TODO Removes the first occurrence of the specified element from this list, if it is present.
     * Returns true if it was present in the list, otherwise false.
     */
    bool remove(const T &e)
    {
	    if(!contains(e))
		    return false;
	    else
	    {
		    T * posi = 0;
		    for(T * p = op; p != ed; p++) if(*p == e)
		    {
			    posi = p;
			    break;
		    }
		    removeIndex(posi - op);
	    }
    }

    /**
     * TODO Replaces the element at the specified position in this list with the specified element.
     * The index is zero-based, with range [0, size).
     * @throw IndexOutOfBound
     */
    void set(int index, const T &element)
    {
	    if(index < 0 or index >= size()) throw IndexOutOfBound();
	    else
	    {
		    (op + index)->~T();
		    new (op + index) T(element);
	    }
    }

   
    /**
     * TODO Returns an iterator over the elements in this list.
     */
    Iterator iterator()
    {
	    return Iterator(op, op, ed);
    }
    Iterator iterator(int x)
    {
	    return Iterator(op + x, op, ed);
    }
};

#endif
