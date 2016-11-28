/** @file */

#ifndef __LESS_H
#define __LESS_H

#include "ElementNotExist.h"

/**
 * Default Comparator with respect to natural order (operator<).
 
 * Users need to either use the default Comparator or provide their 
 * own Comparator of this kind to use this priority queue.
 * The Comparator should be a class with a public function
 * public: bool operator()(const V&, const V&);
 * overriding the "()" operator.
 
 * The Following Code Example may help you understand 
 * how to use a Comparator and, especially, why we override operator().
 * @code
 * #include <iostream>
 * using namespace std;
 *
 * template <class T, class C = Less<T> >
 *  class Example {
 *      private:
 *          C cmp;
 *      public:
 *          bool compare(const T& a, const T& b) {
 *              return cmp(a, b);
 *          }
 *  };
 *
 *  int main() {
 *      Example<int, Less<int> > example; // Less<int> can be omitted.
 *      cout << example.compare(1, 2) << endl;
 *      cout << example.compare(2, 1) << endl;
 *  }
 * @endcode
 */

template <class V>
class Less
{
public:
    /**
     * TODO Reuturns true if element a is less than element b.     
     */
    bool operator()(const V& a, const V& b) { return a < b; }
};

#endif
