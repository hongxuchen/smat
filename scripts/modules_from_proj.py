#!/usr/bin/env python3

import os
import sys
import argparse
import re
import logging
import shutil
from utilities import config_logger, rm, get_proj_name, load_from_records

SUFFIXES = {".c", ".cpp", ".cc"}
I_PREFIXES = {".", "@", "CMakeFiles", "demo"}
I_SUBWORDS = {"test", "fuzz", "regress"}


def parse_args():
    parser = argparse.ArgumentParser(
        description="utility to get modules based on directory structure",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter
    )
    parser.add_argument(
        "-i", "--input",
        dest="infpath",
        required=True,
        help="input filepath for all the projects"
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


def gen_d2f_walk(indir):
    for root, dirs, files in os.walk(indir, topdown=True):
        findir = []
        for fname in files:
            if is_interesting_file(fname):
                findir.append(fname)
            else:
                logger.debug("{} ignored".format(os.path.join(root, fname)))
        logger.debug("===> {}".format(findir))
        if len(findir) != 0:
            dir2files[root] = findir
        dirs[:] = [d for d in dirs if is_interesting_dir(d)]


def gen_d2f(indir, dir2files):
    files = []
    for fname in os.listdir(indir):
        fpath = os.path.abspath(os.path.join(indir, fname))
        if os.path.isdir(fpath):
            if is_interesting_dir(fpath):
                gen_d2f(fpath, dir2files)
            else:
                logger.debug("DIR: {} ignored".format(fpath))
        else:
            if is_interesting_file(fpath):
                files.append(fname)
            else:
                logger.debug("{} ignored".format(fpath))
    if len(files) != 0:
        dir2files[indir] = files

def copy_modules(indir, dir2files, outdir):
    if os.path.isdir(outdir):
        logger.info("==> cleanning outdir: {}".format(outdir))
        rm(outdir)
    for k, v in dir2files.items():
        rel_k = os.path.relpath(k, indir)
        assert (not rel_k.startswith(os.path.sep) and not rel_k.startswith("."))
        out_k = rel_k.replace(os.path.sep, "_")
        out_dir = os.path.join(outdir, out_k)
        logger.debug(" ===> " + out_dir)
        os.makedirs(out_dir, exist_ok=False)
        for vi in v:
            in_fpath = os.path.join(k, vi)
            out_fpath = os.path.join(out_dir, vi)
            shutil.copyfile(in_fpath, out_fpath)
            # print(out_fpath)

def get_list_from_infile(fpath):
    flist = []
    with open(fpath, "r") as infile:
        for line in infile.readlines():
            line = line.rstrip("\n")
            flist.append(line)
    return flist


def copy_based_on_list(infpath, outdir):
    summary = {}
    proj2vers = load_from_records(infpath)
    for proj_name in proj2vers:
        for fpath in proj2vers[proj_name]:
            ver = os.path.basename(fpath)
            modular_proj = proj_name + "-" + ver
            out_dir = os.path.join(outdir, modular_proj)
            dir2files = dict()
            gen_d2f(fpath, dir2files)
            copy_modules(fpath, dir2files, out_dir)
            summary[modular_proj] = len(dir2files)
    mod_len = sum([l for l in summary.values()])
    print("projects:{}, modules: {}".format(len(summary), mod_len))



def dump(dir2files):
    i = 0
    for k, v in dir2files.items():
        i += 1
        print("{})\t {}\t{}".format(i, k, len(v)))


if __name__ == "__main__":
    dir2files = dict()
    logger = config_logger()
    args = parse_args()
    if os.path.isdir(args.infpath):
        gen_d2f(args.infpath, dir2files)
        copy_modules(args.indir, dir2files, args.outdir)
    elif os.path.isfile(args.infpath):
        logger.info(f"copy based on file list, reading {args.infpath}")
        copy_based_on_list(args.infpath, args.outdir)
