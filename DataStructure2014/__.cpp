#ifndef __ARRAYLIST_H
#define __ARRAYLIST_H
 
#include "IndexOutOfBound.h"
#include "ElementNotExist.h"
 
template <class T>
class ArrayList
{
private:
T *elem;
int maxSize,currentLength;
 
void doubleSpace();
public:
class Iterator
{
private:
ArrayList<T> *loction;
int cur;
int last;
public:
Iterator(ArrayList<T> *a):loction(a)
{
cur = 0;
last = -1;
}
 
bool hasNext()
{
return cur < loction -> size();
}
 
const T &next()
{
if (!hasNext()) throw ElementNotExist();
last = cur;
cur ++;
return (loction -> elem[last]);
}
 
void remove()
{
if (last == -1) throw ElementNotExist();
loction -> removeIndex(last);
last--;
}
};
 
ArrayList();
 
~ArrayList();
 
ArrayList(const ArrayList &x);
 
ArrayList & operator = (const ArrayList &x);
 
bool add(const T & e);
 
void add(int index, const T& element);
 
void clear();
 
bool contains(const T& e) const;
 
const T& get(int index) const;
 
bool isEmpty() const;
 
void removeIndex(int index);
 
bool remove(const T &e);
 
void set(int index, const T& element);
 
int size() const;
 
Iterator iterator();
};
 
template <class T>
void ArrayList<T>::doubleSpace()
{
T *tmp = elem;
elem = new T[2 * maxSize];
for (int i = 0;i < maxSize; i++)
elem[i] = tmp[i];
maxSize = 2 * maxSize;
delete []tmp;
}
template <class T>
ArrayList<T>::ArrayList()
{
maxSize = 1000;
elem = new T[maxSize];
currentLength = 0;
}
template <class T>
ArrayList<T>::~ArrayList()
{
delete []elem;
}
 
template <class T>
ArrayList<T>::ArrayList(const ArrayList<T> &x)
{
maxSize = x.maxSize;
elem = new T [maxSize];
currentLength = x.currentLength;
for (int i = 0; i <= currentLength; i++)
elem[i] = x.elem[i];
}
 
template <class T>
ArrayList<T> & ArrayList<T>::operator = (const ArrayList<T> &x)
{
delete [] elem;
maxSize = x.maxSize;
elem = new T[maxSize];
currentLength = x.currentLength;
for (int i = 0; i <= currentLength; i++)
elem[i] = x.elem[i];
}
 
template <class T>
bool ArrayList<T>::add(const T & e)
{
if (currentLength >= maxSize) doubleSpace();
elem[++currentLength] = e;
return 1;
}
 
template <class T>
void ArrayList<T>::add(int index, const T& element)
{
if (!(index >= 0 && index <= currentLength)) throw IndexOutOfBound();
if (currentLength >= maxSize) doubleSpace();
for (int i = currentLength; i >index; i++)
elem[i] = elem[i - 1];
elem[index] = element;
currentLength++;
}
 
template <class T>
void ArrayList<T>::clear()
{
currentLength = 0;
}
 
template <class T>
bool ArrayList<T>::contains(const T& e) const
{
for (int i = 0; i < currentLength; i++)
if (elem[i] == e) return 1;
return 0;
}
 
template <class T>
const T& ArrayList<T>::get(int index) const
{
if (!(index >= 0 && index < currentLength)) throw IndexOutOfBound();
return elem[index];
}
 
template <class T>
bool ArrayList<T>::isEmpty() const
{
return (currentLength == 0);
}
 
template <class T>
void ArrayList<T>::removeIndex(int index)
{
if (!(index >= 0 && index < currentLength)) throw IndexOutOfBound();
for (int i = currentLength - 1; i > index; i--)
elem[i - 1] = elem[i];
currentLength--;
return elem[index];
}
 
template <class T>
bool ArrayList<T>::remove(const T &e)
{
bool flag = 0;
int i;
for (i = 0; i < currentLength; i++)
if (elem[i] == e)
{
break;
flag = 1;
}
removeIndex(i);
return flag;
}
template <class T>
void ArrayList<T>::set(int index, const T& element)
{
if (!(index >= 0 && index < currentLength)) throw IndexOutOfBound();
elem[index] = element;
}
 
template <class T>
int ArrayList<T>::size() const
{
return currentLength;
}
 
template <class T>
typename ArrayList<T>::Iterator ArrayList<T>::iterator()
{
return Iterator(this);
}
#endif