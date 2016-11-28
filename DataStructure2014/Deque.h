/** @file */
#ifndef __DEQUE_H
#define __DEQUE_H

#include "ElementNotExist.h"
#include "IndexOutOfBound.h"
#include "__xyz2606__memory.h"
/**
 * An deque is a linear collection that supports element insertion and removal at both ends.
 * The name deque is short for "double ended queue" and is usually pronounced "deck".
 * Remember: all functions but "contains" and "clear" should be finished in O(1) time.
 *
 * You need to implement both iterators in proper sequential order and ones in reverse sequential order. 
 */
template <class T>
class Deque
{
	T * cop, * ced, * op, * ed;
public:
    class Iterator
    {
	    T * p, *&op, *&ed;
	    bool DEL;
	    bool FRD;
    public:
        /**
         * TODO Returns true if the iteration has more elements.
         */
	Iterator(T *& op, T *& ed, bool FRD) : op(op), ed(ed), FRD(FRD), DEL(false) {if(FRD) p = op; else p = ed;}
        bool hasNext()
	{
		return FRD?(p != ed):(p != op);
	}

        /**
         * TODO Returns the next element in the iteration.
         * @throw ElementNotExist exception when hasNext() == false
         */
        const T &next()
	{
		if(!hasNext()) throw ElementNotExist();
		else
		{
			DEL = true;
			if(FRD)
				return *p++;
			else
				return *--p;
		}
	}

        /**
         * TODO Removes from the underlying collection the last element
         * returned by the iterator
         * The behavior of an iterator is unspecified if the underlying
         * collection is modified while the iteration is in progress in
         * any way other than by calling this method.
         * @throw ElementNotExist
         */
        void remove()
	{
		if(!DEL) throw ElementNotExist();
		else
		{
			DEL = false;
			if(FRD)
			{
				for(T * q = p; q != ed; q++) new (q - 1) T(*q);
				p--;
				ed--;
			}else
			{
				for(T * q = p; q != op; q--) new (q) T(*(q-1));
				p++;
				op++;
			}
		}
	}
    };

    /**
     * TODO Constructs an empty deque.
     */
    Deque()
    {
	    cop = ced = op = ed = 0;
    }
    int capacity() const
    {
	    return ced - cop;
    }
    void deallocate()
    {
	    if(!cop) return;
	    else delete [] cop;
    }
    void reallocate()
    {
	    int siz = capacity()?capacity():1;
	    int pp = op - cop, ep = ed - cop;
	    T * newcop = new T[3 * siz];
	    uninitialized_copy(cop, ced, newcop + siz);
	    deallocate();
	    cop = newcop;
	    ced = cop + siz * 3;
	    op = cop + siz + pp;
	    ed = cop + siz + ep;
    }
    /**
     * TODO Destructor
     */
    ~Deque()
    {
	    deallocate();
    }

    /**
     * TODO Assignment operator
     */
    Deque& operator=(const Deque& x)
    {
	    deallocate();
	    cop = new T[x.capacity()];
	    uninitialized_copy(x.cop, x.ced, cop);
	    op = cop + (x.op - x.cop);
	    ed = cop + (x.ed - x.cop);
	    ced = cop + (x.ced - x.cop);
    }

    /**
     * TODO Copy-constructor
     */
    Deque(const Deque& x)
    {
	    cop = new T[x.capacity()];
	    uninitialized_copy(x.cop, x.ced, cop);
	    op = cop + (x.op - x.cop);
	    ed = cop + (x.ed - x.cop);
	    ced = cop + (x.ced - x.cop);
    }
	
	/**
	 * TODO Inserts the specified element at the front of this deque. 
	 */
	void addFirst(const T& e)
	{
		if(cop == op)
			reallocate();
		op--;
		new (op) T(e);
	}

	/**
	 * TODO Inserts the specified element at the end of this deque.
	 */
	void addLast(const T& e)
	{
		if(ced == ed)
			reallocate();
		new (ed) T(e);
		ed++;
	}

	/**
	 * TODO Returns true if this deque contains the specified element.
	 */
	bool contains(const T& e) const
	{
		for(T * p = op; p != ed; p++) if(*p == e) return true;
		return false;
	}

	/**
	 * TODO Removes all of the elements from this deque.
	 */
	 void clear()
	 {
		 deallocate();
		 op = cop = ed = ced = 0;
	 }

	 /**
	  * TODO Returns true if this deque contains no elements.
	  */
	bool isEmpty() const
	{
		return op == ed;
        }

	/**
	 * TODO Retrieves, but does not remove, the first element of this deque.
	 * @throw ElementNotExist
	 */
	 const T& getFirst()
	 {
		 if(isEmpty()) throw ElementNotExist();
		 else return *op;
	 }

	 /**
	  * TODO Retrieves, but does not remove, the last element of this deque.
	  * @throw ElementNotExist
	  */
	 const T& getLast()
	 {
		 if(isEmpty()) throw ElementNotExist();
		 else return *(ed - 1);
	 }

	 /**
	  * TODO Removes the first element of this deque.
	  * @throw ElementNotExist
	  */
	void removeFirst()
	{
		if(isEmpty()) throw ElementNotExist();
		else
		{
			op->~T();
			op++;
		}
	}

	/**
	 * TODO Removes the last element of this deque.
	 * @throw ElementNotExist
	 */
	void removeLast()
	{
		if(isEmpty()) throw ElementNotExist();
		else
		{
			(ed - 1)->~T();
			ed--;
		}
	}

	/**
	 * TODO Returns a const reference to the element at the specified position in this deque.
	 * The index is zero-based, with range [0, size).
	 * @throw IndexOutOfBound
	 */
	const T& get(int index) const
	{
		if(index < 0 or index >= size()) throw IndexOutOfBound();
		else
			return *(op+index);
		
	}
	
	/**
	 * TODO Replaces the element at the specified position in this deque with the specified element.
	 * The index is zero-based, with range [0, size).
	 * @throw IndexOutOfBound
	 */
	void set(int index, const T& e)
	{
		if(index < 0 or index >= size()) throw IndexOutOfBound();
		else
		{
			(op + index)->~T();
			new (op + index) T(e);
		}
	}

	/**
	 * TODO Returns the number of elements in this deque.
	 */
	 int size() const
	 {
		 return ed - op;
	 }

	 /**
	  * TODO Returns an iterator over the elements in this deque in proper sequence.
	  */
	 Iterator iterator()
	 {
		 return Iterator(op, ed, true);
	 }

	 /**
	  * TODO Returns an iterator over the elements in this deque in reverse sequential order.
	  */
	 Iterator descendingIterator()
	 {
		 return Iterator(op, ed, false);
	 }
};

#endif
