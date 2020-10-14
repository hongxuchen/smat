#!/usr/bin/env python3

import os
import sys
from git import Repo, GitError
import argparse
import re
import shutil
from utilities import config_logger, rm, get_proj_name
from collections import defaultdict

recent_releases = 10
record_fpath = "records.json"

I_TAGS = {"alpha", "beta", "rc", "pre", "post"}
I_FILES = {"BAK"}


# input: in_dir with a few git projects
# outpout: out_dir/proj/versions


def parse_args():
    parser = argparse.ArgumentParser(
        description="utility to create releases based on git tags",
        formatter_class=argparse.ArgumentDefaultsHelpFormatter
    )
    parser.add_argument(
        "-i", "--indir",
        dest="indir",
        required=True,
        help="input directory for all the projects"
    )
    parser.add_argument(
        "-o", "--outdir",
        dest="outdir",
        required=True,
        help="output directory for generated projects"
    )
    return parser.parse_args()

def should_keep(tag):
    tag_name = tag.name.lower()
    exists_ignored = any(i in tag_name for i in I_TAGS)
    return not exists_ignored


def get_git_sorted_tags(repo: Repo):
    sorted_tags = sorted(repo.tags, key=lambda t: t.commit.committed_datetime, reverse=True)
    sorted_tags = list(filter(should_keep, sorted_tags))
    return sorted_tags


def normalize_tag(tag):
    normalized = tag.replace("_", ".")
    return normalized


def copy_releases(repo: Repo, tags, dest_par):
    cur_commit = repo.head.object.hexsha
    recorded_tag_num = min(len(tags), recent_releases)
    recorded_tags = tags[:recorded_tag_num]
    repo_dir = os.path.abspath(os.path.join(repo.common_dir, os.pardir))
    pairs = []
    for tag in recorded_tags:
        tc = tag.commit
        time_str = tc.committed_datetime.strftime("%Y%d%m-%H:%M%S")
        hexsha = tc.hexsha
        repo.git.checkout(tag)
        tag_name = normalize_tag(tag.name)
        logger.info("{:<20} {:>42}\t{}".format(tag_name, hexsha, time_str))
        dest_repo = os.path.join(dest_par, tag_name)
        try:
            shutil.copytree(src=repo_dir, dst=dest_repo, ignore_dangling_symlinks=True)
        except shutil.Error as e:
            for src, dst, error in e.args[0]:
                if not os.path.islink(src):
                    raise
                else:
                    linkto = os.readlink(src)
                    if os.path.exists(linkto):
                        raise

        logger.debug("{} => {}".format(repo_dir, dest_repo))
        pairs.append((dest_repo, tag_name))
    repo.git.checkout(cur_commit)
    return pairs


def get_repo_maps(indir, outdir):
    """get maps for mappings of (in, out) dirs for repos in indir"""
    if os.path.exists(outdir):
        logger.info("cleaning outdir:\t{}".format(outdir))
        rm(outdir)
    repo_maps = {}
    for d in os.listdir(indir):
        in_repo = os.path.abspath(os.path.join(indir, d))
        if d.startswith('.'):
            continue
        if not os.path.isdir(in_repo):
            logger.info("{} not dir".format(d))
        else:
            out_repo = os.path.join(outdir, d)
            os.makedirs(out_repo, exist_ok=False)
            repo_maps[in_repo] = out_repo
    return repo_maps

def dump_records(fpath, data):
    from utility import dump_to_json
    dump_to_json(fpath, data)
    logger.info(f"records saved into \"{fpath}\"")

def main():
    args = parse_args()
    repo_maps = get_repo_maps(args.indir, args.outdir)
    proj2tag = dict()
    for (in_repo, out_repo) in repo_maps.items():
        try:
            print(f"\nanalyzing {in_repo}")
            repo = Repo(in_repo)
            sorted_tags = get_git_sorted_tags(repo)
            pairs = copy_releases(repo, sorted_tags, out_repo)
            proj_name = get_proj_name(in_repo)
            proj2tag[proj_name] = pairs
        except GitError as e:
            print("Exception on {}, {}".format(in_repo, type(e)))
    dump_records(record_fpath, proj2tag)



if __name__ == "__main__":
    logger = config_logger()
    main()
