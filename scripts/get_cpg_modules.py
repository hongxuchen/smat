#!/usr/bin/env python3

import os
import sys
import argparse
import re
import logging
import shutil
from utilities import config_logger

SUFFIXES = {".c", ".cpp", ".cc"}
I_PREFIXES = {".", "@", "CMakeFiles", "demo"}
I_SUBWORDS = {"test"}

logger = config_logger()


def parse_args():
    parser = argparse.ArgumentParser(
        description="utility to get modules based on directory structure",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter
    )
    parser.add_argument(
        "-i", "--indir",
        dest="indir",
        required=True,
        help="input directory for all the projects"
    )
    parser.add_argument(
        "-o", "--utdir",
        dest="outdir",
        required=True,
        help="output directory for generated projects"
    )
    return parser.parse_args()


def _is_ignored_fprefix(fname):
    exists_i_prefixes = any(fname.startswith(i) for i in I_PREFIXES)
    if exists_i_prefixes:
        return True
    exists_i_subwords = any(i in fname for i in I_SUBWORDS)
    if exists_i_subwords:
        return True
    return False

def is_interesting_dir(fpath):
    fname = os.path.basename(fpath)
    return not _is_ignored_fprefix(fname)

def is_interesting_file(fpath):
    fname = os.path.basename(fpath)
    if _is_ignored_fprefix(fname):
        return False
    fprefix, fext = os.path.splitext(fname)
    fprefix = fprefix.lower()
    fext = fext.lower()
    if fext not in SUFFIXES:
        return False
    return True


dir2files = dict()


def gen_d2f_walk(indir):
    for root, dirs, files in os.walk(indir, topdown=True):
        findir = []
        for fname in files:
            print("=== " + fname)
            if is_interesting_file(fname):
                findir.append(fname)
            else:
                logger.debug("{} ignored".format(os.path.join(root, fname)))
        print("===> {}".format(findir))
        if len(findir) != 0:
            dir2files[root] = findir
        dirs[:] = [d for d in dirs if is_interesting_dir(d)]


def gen_d2f(indir):
    files = []
    for fname in os.listdir(indir):
        fpath = os.path.abspath(os.path.join(indir, fname))
        if os.path.isdir(fpath):
            if is_interesting_dir(fpath):
                gen_d2f(fpath)
            else:
                logger.debug("DIR: {} ignored".format(fpath))
        else:
            if is_interesting_file(fpath):
                files.append(fname)
            else:
                logger.debug("{} ignored".format(fpath))
    if len(files) != 0:
        dir2files[indir] = files



def dump(dir2files):
    for k, v in dir2files.items():
        print("\n=== {}:\t{}".format(k, v))



def main():
    args = parse_args()
    gen_d2f(args.indir)
    dump(dir2files)


if __name__ == "__main__":
    main()
