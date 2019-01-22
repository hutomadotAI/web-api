#!/usr/bin/env python
"""Script to build code"""
import argparse
import os
from pathlib import Path
import subprocess

import hu_build.build_docker
from hu_build.build_docker import DockerImage

SCRIPT_PATH = Path(os.path.dirname(os.path.realpath(__file__)))
ROOT_DIR = SCRIPT_PATH.parent.parent


def maven_build(path, modules, skip, clean):
    """Does a maven build"""
    if skip:
        return
    os.chdir(str(path))
    cmdline = ["mvn", "-pl", ",".join(modules)]
    if clean:
        cmdline.extend(["clean"])
    cmdline.extend(["package"])
    subprocess.run(cmdline, cwd=str(path), check=True)


def main(build_args):
    """Main function"""
    component = build_args.component
    tag = build_args.tag_version
    clean = build_args.clean
    skip_maven = build_args.skip_maven
    service_path = ROOT_DIR / "service"
    REGISTRY = 'eu.gcr.io/hutoma-backend'

    if component == "ctrl":
        image = "api/controller"
        maven_build(
            service_path, ["common-lib", "controller-service"],
            skip=skip_maven,
            clean=clean)
        docker_image = DockerImage(
            service_path / "controller-service",
            image,
            image_tag=tag,
            registry=REGISTRY)

    elif component == "svc":
        image = "api/service"
        maven_build(
            service_path, ["common-lib", "core-service"],
            skip=skip_maven,
            clean=clean)
        docker_image = DockerImage(
            service_path / "core-service",
            image,
            image_tag=tag,
            registry=REGISTRY)
    elif component == "db":
        image = "api/db"
        docker_image = DockerImage(
            ROOT_DIR / "db", image, image_tag=tag, registry=REGISTRY)
    else:
        raise Exception("Invalid choice of component {}".format(component))

    hu_build.build_docker.build_single_image("api-{}".format(component),
                                             docker_image, build_args.push)


if __name__ == "__main__":
    PARSER = argparse.ArgumentParser(
        description='API docker build command-line')
    PARSER.add_argument(
        'component',
        help='API component to build',
        choices=['ctrl', 'svc', 'db'])
    PARSER.add_argument(
        'tag_version', help='Docker tag version', default='latest')
    PARSER.add_argument(
        '--skip-maven', help='Skip maven builds', action="store_true")
    PARSER.add_argument(
        '--clean', help='Clean maven builds', action="store_true")
    PARSER.add_argument(
        '--push', help='Push docker images to GCR', action="store_true")
    BUILD_ARGS = PARSER.parse_args()
    main(BUILD_ARGS)
