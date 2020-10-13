import logging

def config_logger():
    logger = logging.getLogger()
    handler = logging.StreamHandler()
    # formatter = logging.Formatter('%(levelname)-6s %(message)s')
    formatter = logging.Formatter('%(message)s')
    handler.setFormatter(formatter)
    logger.addHandler(handler)
    logger.setLevel(logging.INFO)
    return logger

