#include "LinkedList.h"
#include "ArrayList.h"
#include "HashMap.h"
#include "TreeMap.h"
#include "Deque.h"
#include "PriorityQueue.h"
#include "ElementNotExist.h"
#include "IndexOutOfBound.h"
#include "queue"
using namespace std;
typedef pair<int, int> PII;

template <class T>
void printDeque(Deque<T> q)
{
    typename Deque<T>::Iterator itr1 = q.iterator();
    while (itr1.hasNext())
    {
        cout << itr1.next() << "\t";
    }
    cout << endl;
    typename Deque<T>::Iterator itr2 = q.descendingIterator();
    while (itr2.hasNext())
    {
        cout << itr2.next() << "\t";
    }
    cout << endl;
    cout << "End print" << endl;
}

void testDeque()
{
    Deque<int> d1;
    for(int i=0;i<=20;i++)
        d1.addFirst(i);
    printDeque(d1);
    for(int i=1;i<=20;i++)
        d1.removeLast();
    d1.set(0,0);
    Deque<int> d2(d1);
    cout<<d1.isEmpty()<<endl;
    d1.clear();
    cout<<d1.isEmpty()<<endl;
    printDeque(d2);
    for(int i=0;i<=20;i++)
        d1.addLast(i);
    printDeque(d1);
    for(int i=0;i<=20;i++)
        d1.removeFirst();
    cout<<d1.isEmpty()<<endl;
    for(int i=1;i<=100000;i++)
        d2.addLast(i);
    for(int i=0;i<=100000;i++)
        d2.addFirst(i);
    for(int i=0;i<d2.size();i++)
        if(d2.get(i)!=d2.get(d2.size()-i-1))
        {
            cout<<"WA"<<endl;
            return;
        }
    for(int i=0;i<d2.size();i++)d2.set(i,-1);
    if(d2.getFirst()!=d2.getLast())
    {
        cout<<"WA"<<endl;
        return;
    }
    if(!d2.contains(-1))
    {
        cout<<"WA"<<endl;
        return;
    }
    d2.clear();
    if(d2.contains(-1))
    {
        cout<<"WA"<<endl;
        return;
    }
    d2.addFirst(101);
    d2.addLast(202);
    d1=d2;
    printDeque(d1);
    cout<<"done"<<endl;
}

template <class V, class C>
void printHeap(PriorityQueue<V, C> q)
{
    typename PriorityQueue<V, C>::Iterator itr = q.iterator();
    vector<V> s;
    while (itr.hasNext())
    {
        s.push_back(itr.next());
    }
    sort(s.begin(), s.end());
    for (int i = 0; i < s.size(); ++i)
    {
        cout << s[i] << '\t';
    }
    cout << endl;
    cout << "End print" << endl;
}

void testHeap()
{
    ArrayList<int> x;
    PriorityQueue<int> y;
    priority_queue<int> s;
    for(int i=1;i<=20;i++)
    {
        int temp=i/2;
        x.add(temp);
        s.push(temp);
        y.push(temp);
    }
    PriorityQueue<int> z(x);
    PriorityQueue<int> t(z);
    for(int i=1;i<20;i++)
    {
        if(y.front()==z.front())
        {
            cout<<y.front()<<'\t';
        }else
        {
            puts("WA");
            return;
        }
        y.pop();
        z.pop();
    }
    cout<<endl;
    if(y.front()!=s.top() || z.front()!=s.top())
    {
        puts("WA");
        return;
    }
    cout<<y.size()<<endl;
    cout<<z.size()<<endl;
    cout<<t.size()<<endl;
    y.pop();
    z=t;
    z.clear();
    cout<<y.empty()<<endl;
    cout<<z.empty()<<endl;
    try
    {
        auto it=y.iterator();
        it.remove();
    }catch (ElementNotExist e)
    {
        cout<<"OK"<<endl;
    }
    z=t;
    try
    {
        auto it=z.iterator();
        it.next();
        it.remove();
        it.remove();
    }catch (ElementNotExist e)
    {
        cout<<"OK"<<endl;
    }
    y=t;
    z=t;
    printHeap(y);
    printHeap(z);
    auto it=y.iterator();
    while(it.hasNext())
    {
        it.next();
        it.remove();
    }
    cout<<y.empty()<<endl;
}

int main()
{
    testDeque();
    testHeap();
}
