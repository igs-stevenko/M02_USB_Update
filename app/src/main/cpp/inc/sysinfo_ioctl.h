#ifndef __SYSINFO_IOCTL_H__
#define __SYSINFO_IOCTL_H__

struct system_info {
	
	unsigned char mac[16];
	unsigned char proj_name[128];
	unsigned char git_ver[6];
};

#define MAGIC_NUM       	0x12

#define GET_SYSINFO		_IOWR(MAGIC_NUM, 0, struct system_info)

#endif
