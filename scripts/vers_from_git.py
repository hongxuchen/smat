#!/usr/bin/env python3

import os
import sys
from git import Repo, GitError
import argparse
import re
import logging
import shutil

recent_releases = 10
record_file = "versions.txt"

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


def get_git_sorted_tags(repo: Repo):
    sorted_tags = sorted(repo.tags, key=lambda t: t.commit.committed_datetime, reverse=True)
    return sorted_tags


def normalize_tag(tag):
    normalized = tag.replace("_", ".")
    return normalized


def copy_releases(repo: Repo, tags, dest_par):
    recorded_tag_num = min(len(tags), recent_releases)
    recorded_tags = tags[:recorded_tag_num]
    repo_dir = os.path.abspath(os.path.join(repo.common_dir, os.pardir))
    for tag in recorded_tags:
        tc = tag.commit
        time_str = tc.committed_datetime.strftime("%Y%d%m-%H:%M%S")
        hexsha = tc.hexsha
        repo.git.checkout(tag)
        tag_name = normalize_tag(tag.name)
        logger.info("{:<12} {:>42}\t{}".format(tag_name, hexsha, time_str))
        dest_repo = os.path.join(dest_par, tag_name)
        shutil.copytree(repo_dir, dest_repo)
        logger.debug("{} => {}".format(repo_dir, dest_repo))


def rm(path):
    """ param <path> could either be relative or absolute. """
    if os.path.isfile(path) or os.path.islink(path):
        os.remove(path)  # remove the file
    elif os.path.isdir(path):
        shutil.rmtree(path)  # remove dir and all contains
    else:
        raise ValueError("file {} is not a file or dir.".format(path))


def get_repo_maps(indir, outdir):
    if os.path.exists(outdir):
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

def config_logger():

    logger = logging.getLogger()
    handler = logging.StreamHandler()
    # formatter = logging.Formatter('%(levelname)-6s %(message)s')
    formatter = logging.Formatter('%(message)s')
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.setLevel(logging.INFO)
    return logger


def main():
    args = parse_args()
    repo_maps = get_repo_maps(args.indir, args.outdir)
    for (in_repo, out_repo) in repo_maps.items():
        try:
            repo = Repo(in_repo)
            sorted_tags = get_git_sorted_tags(repo)
            copy_releases(repo, sorted_tags, out_repo)
        except GitError as e:
            print("Exception on {}, {}".format(in_repo, type(e)))

logger = config_logger()
main()
