#  Copyright 2021 Collate
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#  http://www.apache.org/licenses/LICENSE-2.0
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.

"""
Data Insigt utility for the metadata CLI
"""
import pathlib
import sys
import traceback

from metadata.config.common import load_config_file
from metadata.utils.logger import cli_logger
from metadata.workflow.data_insight import DataInsightWorkflow
from metadata.workflow.workflow_output_handler import (
    WorkflowType,
    print_data_insight_status,
    print_init_error,
)

logger = cli_logger()


def run_insight(config_path: str) -> None:
    """
    Run the Data Insigt workflow from a config path
    to a JSON or YAML file
    :param config_path: Path to load JSON config
    """

    config_file = pathlib.Path(config_path)
    config_dict = None
    try:
        config_dict = load_config_file(config_file)
        workflow = DataInsightWorkflow.create(config_dict)
        logger.debug(f"Using config: {workflow.config}")
    except Exception as exc:
        logger.debug(traceback.format_exc())
        print_init_error(exc, config_dict, WorkflowType.INSIGHT)
        sys.exit(1)

    workflow.execute()
    workflow.stop()
    print_data_insight_status(workflow)
    workflow.raise_from_status()
