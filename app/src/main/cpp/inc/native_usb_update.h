//
// Created by stevenko on 2023/10/12.
//

#ifndef USB_UPDATE_NATIVE_USB_UPDATE_H
#define USB_UPDATE_NATIVE_USB_UPDATE_H
extern "C"{
int need_update(void);
void do_chmod(const char*);
};


#endif //USB_UPDATE_NATIVE_USB_UPDATE_H
