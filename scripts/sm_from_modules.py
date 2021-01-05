import os
import sys
import argparse
import subprocess

# TODO use multithreads


if len(sys.argv) < 2:
    print("usage: {} MOD_DIR".format(sys.argv[0], file=sys.stderr))
    sys.exit(1)

mod_d = os.path.abspath(sys.argv[1])

rootdir = os.path.abspath(os.path.dirname(os.path.dirname(__file__)))
smat_path = os.path.join(rootdir, "smat.sh")
if not os.path.isfile(smat_path):
    print("ERROR: {} not a file".format(smat_path))
    sys.exit(1)

for d in os.listdir(mod_d):
    d = os.path.join(mod_d, d)
    if not os.path.isdir(d):
        print("WARN: {} not a directoty".format(d))
    mod_name = os.path.basename(d)
    cmd_str = "{} --module='{}' {}".format(smat_path, mod_name, d)
    print("CMD: " + cmd_str)
    rc = subprocess.call(cmd_str.split())
    if rc != 0:
        print("failed with rc={}".format(rc))
        sys.exit(1)
