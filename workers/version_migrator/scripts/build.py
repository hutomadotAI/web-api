#!/usr/bin/env python3
"""Script to build code"""
import argparse
import os
from pathlib import Path

import hu_build.build_docker
from hu_build.build_docker import DockerImage

SCRIPT_PATH = Path(os.path.dirname(os.path.realpath(__file__)))
ROOT_DIR = SCRIPT_PATH.parent


def main(build_args):
    """Main function"""
    tag_version = build_args.version
    src_path = ROOT_DIR
    docker_image = DockerImage(
        src_path,
        'api/version_migrator',
        image_tag=tag_version,
        registry='eu.gcr.io/hutoma-backend')
    hu_build.build_docker.build_single_image(
        "version_migrator", docker_image, push=build_args.push)


if __name__ == "__main__":
    PARSER = argparse.ArgumentParser(
        description='Version migrator build command-line')
    PARSER.add_argument('version', help='version number')
    PARSER.add_argument(
        '--push',
        help='Push image to Google Container Registry',
        action="store_true")
    BUILD_ARGS = PARSER.parse_args()
    main(BUILD_ARGS)
