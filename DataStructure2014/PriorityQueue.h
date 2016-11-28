/** @file */
#ifndef __PRIORITYQUEUE_H
#define __PRIORITYQUEUE_H

#include "ArrayList.h"
#include "ElementNotExist.h"
#include "Less.h"

/**
 * This is a priority queue based on a priority priority queue. The
 * elements of the priority queue are ordered according to their 
 * natual ordering (operator<), or by a Comparator provided as the
 * second template parameter.
 * The head of this queue is the least element with respect to the
 * specified ordering (different from C++ STL).
 * The iterator does not return the elements in any particular order.
 * But it is required that the iterator will eventually return every
 * element in this queue (even if removals are performed).
 */

template <class V, class C = Less<V> >
class PriorityQueue
{
    ArrayList<V> array;
    public:
    int size() const
    {
	    return array.size();
    }
    private:
    int ooops(int x = -1)
    {
	    if(x == -1) x = size() - 1;
	    while(x and !C()(array.get((x - 1) / 2), array.get(x)))
	    {
		    array.sawp((x - 1) / 2, x);
		    x = (x - 1) / 2;
	    }
	    return x;
    }
    int spooo(int x, int p = 0)
    {
	    int y, rtn = x;
	    while(x * 2 + 1 < array.size())
	    {
		    if(x * 2 + 2 < array.size() and C()(array.get(x * 2 + 2), array.get(x * 2 + 1))) y = x * 2 + 2; else y = x * 2 + 1;
		    if(!C()(array.get(x), array.get(y))) {array.sawp(x, y); x = y; if(x < p and x > rtn) rtn = x;} else break;
	    }
	    return rtn;
    }
    void makeHeap()
    {
	    for(int i = array.size() - 1; i >= 0; i--) spooo(i);
    }
    int heapDelete(int x = 0, int posi = 0)
    {
	//返回删除itr并用堆尾元素替代后, 堆尾元素移动路径中最后一个处于posi之前的位置
	array.sawp(x, array.size() - 1);
	array.removeIndex(array.size() - 1);
	int itr1 = ooops(x);
	if(itr1 != x)
		return itr1;
	else
		return spooo(x, posi);
    }
public:
    class Iterator
    {
	    int posi, prev, jump;
	    PriorityQueue<V, C> & _;
    public:
    	Iterator(PriorityQueue<V, C> & _) : _(_) {posi = prev = jump = 0;}
        /**
         * TODO Returns true if the iteration has more elements.
         */
        bool hasNext()
	{
		return posi < _.array.size();
	}

        /**
         * TODO Returns the next element in the iteration.
         * @throw ElementNotExist exception when hasNext() == false
         */
        const V &next()
	{
		if(!hasNext()) throw ElementNotExist();
		else
		{
			prev = posi;
			const V & rtn = _.array.get(posi);
			if(jump != posi)
				posi = jump;
			else
				jump = ++posi;
			return rtn;
		}
	}


	/**
	 * TODO Removes from the underlying collection the last element
	 * returned by the iterator.
	 * The behavior of an iterator is unspecified if the underlying
	 * collection is modified while the iteration is in progress in
	 * any way other than by calling this method.
	 * @throw ElementNotExist
	 */
	void remove()
	{
		if(prev == posi)
			throw ElementNotExist();
		else
		{
			int itr = _.heapDelete(prev, posi);
			if(prev == 0)
			{
				prev = posi = jump = 0;
			}else if(posi >= _.array.size())
			{
				prev = jump = posi;
			}else
			{
				jump = posi;
				prev = posi = itr;
			}
		}
	}
	~Iterator()
	{
	}
    };

    
    /**
     * TODO Constructs an empty priority queue.
     */
    PriorityQueue() 
    {
    }

    /**
     * TODO Destructor
     */
    ~PriorityQueue()
    {
    }

    /**
     * TODO Assignment operator
     */
    PriorityQueue &operator=(const PriorityQueue &x)
    {
	    array = x.array;
	    return * this;
    }

    /**
     * TODO Copy-constructor
     */
    PriorityQueue(const PriorityQueue &x)
    {
	    array = x.array;
    }

	/**
	 * TODO Initializer_list-constructor
	 * Constructs a priority queue over the elements in this Array List.
     * Requires to finish in O(n) time.
	 */
    PriorityQueue(const ArrayList<V> &x)
    {
	    array = x;
	    makeHeap();
    }

    /**
     * TODO Returns an iterator over the elements in this priority queue.
     */
    Iterator iterator()
    {
	    return Iterator(*this);
    }

    /**
     * TODO Removes all of the elements from this priority queue.
     */
    void clear()
    {
    	array.clear();
    }

    /**
     * TODO Returns a const reference to the front of the priority queue.
     * If there are no elements, this function should throw ElementNotExist exception.
     * @throw ElementNotExist
     */
    const V &front() const
    {
	    if(!array.size()) throw ElementNotExist();
	    else return array.get(0);
    }

    /**
     * TODO Returns true if this PriorityQueue contains no elements.
     */
    bool empty() const
    {
	    return array.isEmpty();
    }

    /**
     * TODO Add an element to the priority queue.
     */
    void push(const V &value)
    {
	    array.add(value);
	    ooops();
    }

    /**
     * TODO Removes the top element of this priority queue if present.
     * If there is no element, throws ElementNotExist exception.
     * @throw ElementNotExist
     */
    void pop()
    {
	    heapDelete();
    }

    /**
     * TODO Returns the number of key-value mappings in this map.
     */
    
    friend Iterator;
    
};

#endif
