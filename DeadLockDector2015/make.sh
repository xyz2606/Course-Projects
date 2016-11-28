gcc -fPIC -shared -o hook.so hook.c -ldl
gcc -o example example.c -lpthread

LD_PRELOAD=./hook.so ./example
