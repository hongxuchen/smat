import logging
import os
import shutil


def config_logger():
    logger = logging.getLogger()
    handler = logging.StreamHandler()
    # formatter = logging.Formatter('%(levelname)-6s %(message)s')
    formatter = logging.Formatter('%(message)s')
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.setLevel(logging.INFO)
    return logger


def rm(path):
    """ param <path> could either be relative or absolute. """
    if os.path.isfile(path) or os.path.islink(path):
        os.remove(path)  # remove the file
    elif os.path.isdir(path):
        shutil.rmtree(path)  # remove dir and all contains
    else:
        raise ValueError("file {} is not a file or dir.".format(path))


def dump_to_json(fpath, data):
    import json
    if not fpath.endswith(".json"):
        fpath = fpath + ".json"
    with open(fpath, "w") as json_file:
        json.dump(data, json_file, indent=2, sort_keys=True)


def dump_to_toml(fpath, data):
    import toml
    if not fpath.endswith(".toml"):
        fpath = fpath + ".toml"
    with open(fpath, "w") as toml_file:
        s = toml.dumps(data)
        toml_file.write(s)

def get_proj_name(fpath):
    return os.path.basename(fpath)

