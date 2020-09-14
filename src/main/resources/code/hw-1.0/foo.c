#include <stdio.h>

int sink(int);

int foo(int x, int y) {
    if (y < 10)
        goto end;
    if (x < 10) {
        sink(x);
    }
end:
    printf("foo");
    return x;
}
