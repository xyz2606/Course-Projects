/** @file */
#ifndef __TREEMAP_H
#define __TREEMAP_H

#include "ElementNotExist.h"
#include<iostream>
using namespace std;
/**
 * TreeMap is the balanced-tree implementation of map. The iterators must
 * iterate through the map in the natural order (operator<) of the key.
 */
template<class K, class V>
class TreeMap
{
public:
    class Entry
    {
	    public:
        K key;
        V value;
	unsigned int RAN;
	Entry * s[2], *f;
        Entry(K k, V v) : key(k), value(v), RAN(random())
        {
	    s[0] = s[1] = f = 0;   
        }

        const K & getKey() const
        {
            return key;
        }

        const V & getValue() const
        {
            return value;
        }
	Entry * succ()
	{
		//printf("this%d %d %d %d\n", this, s[0], s[1], f);
		if(s[1])
		{
			Entry * q = s[1];
			while(q->s[0]) q = q->s[0];
			return q;
		}else
			return (f and f->s[0] == this)?f:0;
	}
	bool containsValue(const V & v)
	{
		if(value == v) return true;
		else
		{
			for(int i = 0; i < 2; i++) if(s[i] and s[i]->containsValue(v)) return true;
			return false;
		}
	}
	Entry(const Entry & x) : key(x.key), value(x.value), RAN(random())
	{
		f = 0;
		for(int d = 0; d < 2; d++)
			if(x.s[d]) {s[d] = new Entry(*x.s[d]); s[d]->f = this;} else s[d] = 0;
	}
	int getd()
	{
		return f->s[1] == this;
	}
	void rotate(Entry * & root)
	{
		Entry * z = f->f;
		if(f->f) f->f->s[f->getd()] = this; else root = this;
		int d = getd();
		f->f = this;
		f->s[d] = s[d ^ 1];
		if(s[d ^ 1])s[d ^ 1]->f = f;
		s[d ^ 1] = f;
		f = z;
	}
	void _(Entry * & root)
	{
		while(f and f->RAN > RAN) rotate(root);
	}
	void del(Entry * & root)
	{
		while(s[0] or s[1])
		{
			if(!s[0] or s[0]->RAN > s[1]->RAN) s[1]->rotate(root);
			else s[0]->rotate(root);
		}
		if(f) f->s[getd()] = 0;
		delete this;
	}
	~Entry()
	{
		for(int i = 0; i < 2; i++) if(s[i]) delete s[i];
	}
    };

    class Iterator
    {
	    Entry * p;
    public:
        /**
         * TODO Returns true if the iteration has more elements.
         */
	Iterator(Entry * p) : p(p)
	{
		if(p) while(p->s[0]) p = p->s[0];
	}
        bool hasNext()
	{
		return p;
	}

        /**
         * TODO Returns the next element in the iteration.
         * @throw ElementNotExist exception when hasNext() == false
         */
        const Entry &next()
	{
		if(!hasNext()) throw ElementNotExist();
		else
		{
			const Entry & rtn(*p);
			//cout << p->key << endl;
			p = p->succ();
			return rtn;
		}
	}
    };
    Entry * root;
    int siz;

    /**
     * TODO Constructs an empty tree map.
     */
    TreeMap()
    {
	    root = 0;
	    siz = 0;
    }

    /**
     * TODO Destructor
     */
    ~TreeMap()
    {
	    delete root;
    }

    /**
     * TODO Assignment operator
     */
    TreeMap &operator=(const TreeMap &x)
    {
	    if(root) delete root;
	    if(x.siz)
	    {
		    root = new Entry(*x.root);
		    siz = x.siz;
	    }else {root = 0; siz = 0;}
    }

    /**
     * TODO Copy-constructor
     */
    TreeMap(const TreeMap &x)
    {
	    if(x.siz)
	    {
		    root = new Entry(*x.root);
		    siz = x.siz;
	    }else {root = 0; siz = 0;}
    }

    /**
     * TODO Returns an iterator over the elements in this map.
     */
    Iterator iterator() const
    {
	    return Iterator(root);
    }

    /**
     * TODO Removes all of the mappings from this map.
     */
    void clear()
    {
	    delete root;
	    root = 0;
	    siz = 0;
    }

    /**
     * TODO Returns true if this map contains a mapping for the specified key.
     */
    bool containsKey(const K &key) const
    {
	    Entry * p = root;
	    while(p)
	    {
		    if(p->key == key) return true;
		    else p = p->s[p->key < key];
	    }
	    return false;
    }

    /**
     * TODO Returns true if this map maps one or more keys to the specified value.
     */
    bool containsValue(const V &value) const
    {
	    return root->containsValue(value);
    }

    /**
     * TODO Returns a const reference to the value to which the specified key is mapped.
     * If the key is not present in this map, this function should throw ElementNotExist exception.
     * @throw ElementNotExist
     */
    const V &get(const K &key) const
    {
	    Entry * p = root;
	    while(p)
	    {
		    if(p->key == key) return p->getValue();
		    else p = p->s[p->key < key];
	    }
	    throw ElementNotExist();
    }

    /**
     * TODO Returns true if this map contains no key-value mappings.
     */
    bool isEmpty() const
    {
	    return !siz;
    }

    /**
     * TODO Associates the specified value with the specified key in this map.
     */
    void put(const K &key, const V &value)
    {
	    if(!root)
	    {
		    root = new Entry(key, value);
	    	    siz = 1;
	    }
	    else
	    {
		    //cout << "puts" << key << (int)root << endl;
		    Entry * p = root;
		    while(p)
		    {
			    if(p->getKey() == key)
			    {
				    p->value.~V();
				    new (&p->value) V(value);
				    //p->value = value;
				    return;
			    }else
			    {
				    int d = p->getKey() < key;
				    if(!p->s[d])
				    {
					    //printf("%d\n", p);
					    p->s[d] = new Entry(key, value);
					    p->s[d]->f = p;
					    //p->s[d]->_(root);
					    siz++;
					    return;
				    }else p = p->s[d];
			    }
		    }
	    }
    }

    /**
     * TODO Removes the mapping for the specified key from this map if present.
     * If there is no mapping for the specified key, throws ElementNotExist exception.
     * @throw ElementNotExist
     */
    void remove(const K &key)
    {
	    Entry * p = root;
	    while(p)
	    {
		    if(p->key == key) 
		    {
			    p->del(root);
			    siz--;
			    return;
		    }else p = p->s[p->key < key];
	    }
	    throw ElementNotExist();
    }

    /**
     * TODO Returns the number of key-value mappings in this map.
     */
    int size() const
    {
	    return siz;
    }
};

#endif
